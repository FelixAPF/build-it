package com.build_it.buildit.service;

import com.build_it.buildit.dto.*;
import com.build_it.buildit.entity.*;
import com.build_it.buildit.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private final UserRepository userRepository;
  private final WorkerProfileRepository workerProfileRepository;
  private final BusinessProfileRepository businessProfileRepository;
  private final JobPostingRepository jobPostingRepository;
  private final JobApplicationRepository jobApplicationRepository;
  private final ReviewRepository reviewRepository;

  @Transactional(readOnly = true)
  public List<WorkerDashboardResponse> getWorkerDashboard(String email) {
    User user = userRepository.findByEmail(email).orElseThrow();
    WorkerProfile worker = workerProfileRepository.findByUserId(user.getId()).orElseThrow();

    List<JobApplication> applications = jobApplicationRepository.findByWorkerId(worker.getId());

    return applications.stream()
      .map(app -> {
        JobPosting posting = app.getJobRequirement().getJobPosting();

        boolean alreadyReviewed = reviewRepository.existsByReviewerIdAndRevieweeIdAndJobPostingId(
          user.getId(), posting.getBusiness().getUser().getId(), posting.getId()
        );

        String fullAddress = posting.getAddress();
        if (posting.getCity() != null) {
          fullAddress += ", " + posting.getCity() + ", " + posting.getProvince() + " " + posting.getPostalCode();
        }

        return WorkerDashboardResponse.builder()
          .applicationId(app.getId())
          .jobId(posting.getId())
          .companyName(posting.getBusiness().getCompanyName())
// Inside getWorkerDashboard map...
          .companyPhone(posting.getBusiness().getPhoneNumber())
          .companyEmail(posting.getBusiness().getUser().getEmail()) // <-- NEW
          .address(fullAddress)
          .startDatetime(posting.getStartDatetime())
          .endDatetime(posting.getEndDatetime())
          .isTimeFlexible(posting.getIsTimeFlexible())
          .providesSupplyChain(posting.getProvidesSupplyChain()) // <-- NEW
          .specificTools(posting.getSpecificTools()) // <-- NEW
          .jobType(app.getJobRequirement().getJobType().name())
          .supplyChainItems(posting.getSupplyChainItems()) // <-- NEW
          .paymentType(app.getJobRequirement().getPaymentType().name()) // <-- NEW
          .payRate(app.getJobRequirement().getPayRate())
          .answers(app.getJobRequirement().getAnswers().stream().map(a -> new RequirementAnswerDto(a.getQuestion(), a.getAnswer())).toList()) // <-- NEW
          .applicationStatus(app.getStatus().name())
          .jobStatus(posting.getStatus().name())
          .reviewedBusiness(alreadyReviewed)
          .build();
      })
      .sorted((a, b) -> b.getStartDatetime().compareTo(a.getStartDatetime()))
      .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<BusinessDashboardResponse> getBusinessDashboard(String email) {
    User user = userRepository.findByEmail(email).orElseThrow();
    BusinessProfile business = businessProfileRepository.findByUserId(user.getId()).orElseThrow();

    List<JobPosting> postings = jobPostingRepository.findByBusinessIdOrderByStartDatetimeDesc(business.getId());

    return postings.stream()
      .map(posting -> {
        String fullAddress = posting.getAddress();
        if (posting.getCity() != null) {
          fullAddress += ", " + posting.getCity() + ", " + posting.getProvince() + " " + posting.getPostalCode();
        }

        return BusinessDashboardResponse.builder()
          .jobPostingId(posting.getId())
          .address(fullAddress)
          .startDatetime(posting.getStartDatetime())
          .endDatetime(posting.getEndDatetime())
          .isTimeFlexible(posting.getIsTimeFlexible())
          .providesSupplyChain(posting.getProvidesSupplyChain()) // <-- NEW
          .specificTools(posting.getSpecificTools()) // <-- NEW
          .supplyChainItems(posting.getSupplyChainItems()) // <-- NEW
          .status(posting.getStatus().name())
          .requirements(posting.getRequirements().stream()
            .map(req -> {
              long pendingCount = req.getApplications().stream()
                .filter(app -> app.getStatus() == ApplicationStatus.PENDING)
                .count();

              return RequirementDetailDto.builder()
                .requirementId(req.getId())
                .jobType(req.getJobType().name())
                .paymentType(req.getPaymentType().name())
                .payRate(req.getPayRate())
                .qtyRequested(req.getQtyRequested())
                .qtyFilled(req.getQtyFilled())
                .answers(req.getAnswers().stream().map(a -> new RequirementAnswerDto(a.getQuestion(), a.getAnswer())).toList()) // <-- NEW
                .pendingApplicantsCount(pendingCount)
                .assignedWorkers(req.getApplications().stream()
                  .filter(app -> app.getStatus() == ApplicationStatus.SELECTED)
                  .map(app -> {
                    boolean alreadyReviewed = reviewRepository.existsByReviewerIdAndRevieweeIdAndJobPostingId(
                      user.getId(), app.getWorker().getUser().getId(), posting.getId()
                    );
                    return AssignedWorkerDto.builder()
                      .applicationId(app.getId())
                      .workerId(app.getWorker().getId())
                      .fullName(app.getWorker().getFullName())
                      .phoneNumber(app.getWorker().getPhoneNumber())
                      .averageRating(app.getWorker().getAverageRating())
                      .reviewedWorker(alreadyReviewed)
                      .build();
                  })
                  .collect(Collectors.toList()))
                .build();
            })
            .collect(Collectors.toList()))
          .build();
      })
      .collect(Collectors.toList());
  }
}
