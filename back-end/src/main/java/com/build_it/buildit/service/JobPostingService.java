package com.build_it.buildit.service;

import com.build_it.buildit.dto.CreateJobPostingRequest;
import com.build_it.buildit.dto.JobRequirementRequest;
import com.build_it.buildit.entity.*;
import com.build_it.buildit.repository.BusinessProfileRepository;
import com.build_it.buildit.repository.JobPostingRepository;
import com.build_it.buildit.repository.UserRepository;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class JobPostingService {

  private final JobPostingRepository jobPostingRepository;
  private final BusinessProfileRepository businessProfileRepository;
  private final AuditLogService auditLogService;
  private final UserRepository userRepository;

  @Value("${stripe.api.key}")
  private String stripeApiKey;

  @Value("${app.frontend-url}")
  private String frontEndUrl;

  private static final double BASE_FEE_PER_WORKER = 5.00;
  private static final double TPS_RATE = 0.05;
  private static final double TVQ_RATE = 0.09975;

  @Transactional
  public String createJobPosting(CreateJobPostingRequest request, String businessEmail, String dynamicFrontendUrl) {
    Stripe.apiKey = stripeApiKey;

    User user = userRepository.findByEmail(businessEmail)
      .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getStatus() != AccountStatus.ACTIVE) {
      throw new RuntimeException("Account is not active. Pending admin verification.");
    }

    BusinessProfile businessProfile = businessProfileRepository.findByUserId(user.getId())
      .orElseThrow(() -> new RuntimeException("Business profile not found"));

    int totalWorkersRequested = request.getRequirements().stream()
      .mapToInt(JobRequirementRequest::getQtyRequested)
      .sum();

    double subtotal = totalWorkersRequested * BASE_FEE_PER_WORKER;
    double tps = subtotal * TPS_RATE;
    double tvq = subtotal * TVQ_RATE;
    double grandTotal = subtotal + tps + tvq;

    BigDecimal finalFee = new BigDecimal(grandTotal).setScale(2, RoundingMode.HALF_UP);

    JobPosting jobPosting = JobPosting.builder()
      .business(businessProfile)
      .address(request.getAddress())
      .city(request.getCity())
      .province(request.getProvince())
      .postalCode(request.getPostalCode())
      .startDatetime(request.getStartDatetime())
      .endDatetime(request.getEndDatetime())
      .isTimeFlexible(request.getIsTimeFlexible() != null ? request.getIsTimeFlexible() : false)
      .providesSupplyChain(request.getProvidesSupplyChain() != null ? request.getProvidesSupplyChain() : false)
      .specificTools(request.getSpecificTools() != null ? request.getSpecificTools() : new ArrayList<>())
      .supplyChainItems(request.getSupplyChainItems() != null ? request.getSupplyChainItems() : new ArrayList<>())
      .status(JobStatus.PENDING_PAYMENT)
      .totalAppFeeCharged(finalFee.doubleValue())
      .build();

    for (JobRequirementRequest req : request.getRequirements()) {
      JobRequirement requirement = JobRequirement.builder()
        .jobPosting(jobPosting)
        .jobType(req.getJobType())
        .paymentType(req.getPaymentType())
        .payRate(req.getPayRate())
        .qtyRequested(req.getQtyRequested())
        .qtyFilled(0)
        .answers(req.getAnswers() != null ? req.getAnswers().stream()
          .map(a -> new RequirementAnswer(a.getQuestion(), a.getAnswer()))
          .collect(java.util.stream.Collectors.toList()) : new ArrayList<>())
        .build();

      jobPosting.getRequirements().add(requirement);
    }

    jobPosting = jobPostingRepository.save(jobPosting);

    try {
      long amountInCents = finalFee.multiply(new BigDecimal(100)).longValue();

      SessionCreateParams params = SessionCreateParams.builder()
        .setMode(SessionCreateParams.Mode.PAYMENT)
        .setSuccessUrl(dynamicFrontendUrl + "/payment-success?jobId=" + jobPosting.getId())
        .setCancelUrl(dynamicFrontendUrl + "/business-dashboard")
        .putMetadata("jobId", jobPosting.getId().toString()) // <-- CRITICAL: Attach Job ID!
        .addLineItem(
          SessionCreateParams.LineItem.builder()
            .setQuantity(1L)
            .setPriceData(
              SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency("cad")
                .setUnitAmount(amountInCents)
                .setProductData(
                  SessionCreateParams.LineItem.PriceData.ProductData.builder()
                    .setName("BuildIt Matchmaking Fee - " + totalWorkersRequested + " Worker(s)")
                    .setDescription("Job Site: " + request.getAddress())
                    .build())
                .build())
            .build())
        .build();

      Session session = Session.create(params);
      auditLogService.log(businessEmail, "JOB_POSTED", "Created job requirement located at: " + request.getAddress() + ". Awaiting Stripe payment.");
      return session.getUrl();

    } catch (Exception e) {
      throw new RuntimeException("Failed to generate payment session: " + e.getMessage());
    }
  }

  @Transactional
  public String cancelJobPosting(Long jobId, String businessEmail) {
    User user = userRepository.findByEmail(businessEmail)
      .orElseThrow(() -> new RuntimeException("User not found"));
    BusinessProfile business = businessProfileRepository.findByUserId(user.getId())
      .orElseThrow(() -> new RuntimeException("Business profile not found"));

    JobPosting posting = jobPostingRepository.findById(jobId)
      .orElseThrow(() -> new RuntimeException("Job posting not found"));

    if (!posting.getBusiness().getId().equals(business.getId())) {
      throw new RuntimeException("You do not have permission to cancel this job.");
    }

    if (posting.getStatus() == JobStatus.CANCELLED || posting.getStatus() == JobStatus.COMPLETED) {
      throw new RuntimeException("This job cannot be cancelled in its current state.");
    }

    posting.setStatus(JobStatus.CANCELLED);
    jobPostingRepository.save(posting);

    for (JobRequirement req : posting.getRequirements()) {
      for (JobApplication app : req.getAssignedWorkers()) {
        if (app.getStatus() == ApplicationStatus.PENDING || app.getStatus() == ApplicationStatus.SELECTED) {
          app.setStatus(ApplicationStatus.AUTO_CANCELLED);
        }
      }
    }

    auditLogService.log(businessEmail, "JOB_CANCELLED", "Cancelled active job assignment ID: " + jobId + ". Voided dependent applicants.");
    return "Job posting has been successfully cancelled. All worker applications have been voided.";
  }

  @Transactional
  public String generatePaymentSessionForExistingJob(Long jobId, String businessEmail, String dynamicFrontendUrl) {
    Stripe.apiKey = stripeApiKey;

    User user = userRepository.findByEmail(businessEmail)
      .orElseThrow(() -> new RuntimeException("User not found"));
    BusinessProfile business = businessProfileRepository.findByUserId(user.getId())
      .orElseThrow(() -> new RuntimeException("Business profile not found"));

    JobPosting jobPosting = jobPostingRepository.findById(jobId)
      .orElseThrow(() -> new RuntimeException("Job posting not found"));

    if (!jobPosting.getBusiness().getId().equals(business.getId())) {
      throw new RuntimeException("You do not have permission to pay for this job.");
    }
    if (jobPosting.getStatus() != JobStatus.PENDING_PAYMENT) {
      throw new RuntimeException("This job is not pending payment.");
    }

    try {
      long amountInCents = BigDecimal.valueOf(jobPosting.getTotalAppFeeCharged()).multiply(new BigDecimal(100)).longValue();

      int totalWorkersRequested = jobPosting.getRequirements().stream()
        .mapToInt(JobRequirement::getQtyRequested)
        .sum();

      SessionCreateParams params = SessionCreateParams.builder()
        .setMode(SessionCreateParams.Mode.PAYMENT)
        .setSuccessUrl(dynamicFrontendUrl + "/payment-success?jobId=" + jobPosting.getId())
        .setCancelUrl(dynamicFrontendUrl + "/business-dashboard")
        .putMetadata("jobId", jobPosting.getId().toString()) // <-- CRITICAL: Attach Job ID!
        .addLineItem(
          SessionCreateParams.LineItem.builder()
            .setQuantity(1L)
            .setPriceData(
              SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency("cad")
                .setUnitAmount(amountInCents)
                .setProductData(
                  SessionCreateParams.LineItem.PriceData.ProductData.builder()
                    .setName("BuildIt Matchmaking Fee - " + totalWorkersRequested + " Worker(s)")
                    .setDescription("Job Site: " + jobPosting.getAddress())
                    .build())
                .build())
            .build())
        .build();

      Session session = Session.create(params);
      return session.getUrl();

    } catch (Exception e) {
      throw new RuntimeException("Failed to generate payment session: " + e.getMessage());
    }
  }
}
