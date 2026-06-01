package com.build_it.buildit.service;

import com.build_it.buildit.dto.CreateJobPostingRequest;
import com.build_it.buildit.dto.JobRequirementRequest;
import com.build_it.buildit.entity.*;
import com.build_it.buildit.repository.BusinessProfileRepository;
import com.build_it.buildit.repository.JobPostingRepository;
import com.build_it.buildit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class JobPostingService {

  private final JobPostingRepository jobPostingRepository;
  private final BusinessProfileRepository businessProfileRepository;
  private final UserRepository userRepository;

  private static final double BASE_FEE_PER_WORKER = 5.00;
  private static final double TPS_RATE = 0.05;
  private static final double TVQ_RATE = 0.09975;

  @Transactional
  public JobPosting createJobPosting(CreateJobPostingRequest request, String businessEmail) {

    // 1. Authenticate & Verify Business
    User user = userRepository.findByEmail(businessEmail)
      .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getStatus() != AccountStatus.ACTIVE) {
      throw new RuntimeException("Account is not active. Pending admin verification.");
    }

    BusinessProfile businessProfile = businessProfileRepository.findByUserId(user.getId())
      .orElseThrow(() -> new RuntimeException("Business profile not found"));

    // 2. Calculate Pricing based on Cart Items
    int totalWorkersRequested = request.getRequirements().stream()
      .mapToInt(JobRequirementRequest::getQtyRequested)
      .sum();

    double subtotal = totalWorkersRequested * BASE_FEE_PER_WORKER;
    double tps = subtotal * TPS_RATE;
    double tvq = subtotal * TVQ_RATE;
    double grandTotal = subtotal + tps + tvq;

    // Round to 2 decimal places securely
    BigDecimal finalFee = new BigDecimal(grandTotal).setScale(2, RoundingMode.HALF_UP);

    // 3. Create the Header (Chantier)
    JobPosting jobPosting = JobPosting.builder()
      .business(businessProfile)
      .address(request.getAddress())
      .startDatetime(request.getStartDatetime())
      .endDatetime(request.getEndDatetime())
      .status(JobStatus.OPEN)
      .totalAppFeeCharged(finalFee.doubleValue())
      .build();

    // 4. Map the line items (Requirements)
    for (JobRequirementRequest req : request.getRequirements()) {
      JobRequirement requirement = JobRequirement.builder()
        .jobPosting(jobPosting)
        .jobType(req.getJobType())
        .hourlyRate(req.getHourlyRate())
        .qtyRequested(req.getQtyRequested())
        .qtyFilled(0) // Starts at 0
        .build();

      jobPosting.getRequirements().add(requirement);
    }

    // Because we set CascadeType.ALL on the Entity, saving the posting saves the requirements too!
    return jobPostingRepository.save(jobPosting);
  }

  @Transactional
  public String cancelJobPosting(Long jobId, String businessEmail) {
    User user = userRepository.findByEmail(businessEmail)
      .orElseThrow(() -> new RuntimeException("User not found"));
    BusinessProfile business = businessProfileRepository.findByUserId(user.getId())
      .orElseThrow(() -> new RuntimeException("Business profile not found"));

    JobPosting posting = jobPostingRepository.findById(jobId)
      .orElseThrow(() -> new RuntimeException("Job posting not found"));

    // Security check: Make sure this business actually owns the job
    if (!posting.getBusiness().getId().equals(business.getId())) {
      throw new RuntimeException("You do not have permission to cancel this job.");
    }

    // Prevent cancelling jobs that are already finished or cancelled
    if (posting.getStatus() == JobStatus.CANCELLED || posting.getStatus() == JobStatus.COMPLETED) {
      throw new RuntimeException("This job cannot be cancelled in its current state.");
    }

    // 1. Cancel the main Job Posting
    posting.setStatus(JobStatus.CANCELLED);
    jobPostingRepository.save(posting);

    // 2. Cascade the cancellation to all workers who applied or were hired
    for (JobRequirement req : posting.getRequirements()) {
      for (JobApplication app : req.getApplications()) {
        if (app.getStatus() == ApplicationStatus.PENDING || app.getStatus() == ApplicationStatus.SELECTED) {
          app.setStatus(ApplicationStatus.AUTO_CANCELLED);
          // Note: If you don't have jobApplicationRepository injected here,
          // JPA will actually auto-save this change automatically when the
          // transaction commits because the entities are managed!
        }
      }
    }

    // *Future feature note: This is where you would trigger a refund via the Stripe API for the $5 + Tax fee!*

    return "Job posting has been successfully cancelled. All worker applications have been voided.";
  }
}
