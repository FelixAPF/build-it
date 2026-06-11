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
  private final JobPostingRepository jobPostingRepository;
  private final BusinessProfileRepository businessProfileRepository;
  private final JobApplicationRepository jobApplicationRepository;
  private final WorkerProfileRepository workerProfileRepository;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public List<WorkerDashboardResponse> getWorkerDashboard(String email) {
    User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    // FIX: Using findByUserId
    WorkerProfile worker = workerProfileRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException("Profile not found"));

    // FIX: Using findByWorkerId
    return jobApplicationRepository.findByWorkerId(worker.getId()).stream()
      .map(app -> {
        JobPosting posting = app.getJobRequirement().getJobPosting();
        String fullAddress = posting.getAddress() + ", " + posting.getCity() + ", " + posting.getProvince() + " " + posting.getPostalCode();

        return WorkerDashboardResponse.builder()
          .applicationId(app.getId())
          .jobId(posting.getId())
          .companyName(posting.getBusiness().getCompanyName())
          .companyPhone(posting.getBusiness().getPhoneNumber())
          .companyEmail(posting.getBusiness().getUser().getEmail())
          .address(fullAddress)
          .startDatetime(posting.getStartDatetime())
          .endDatetime(posting.getEndDatetime())
          .isTimeFlexible(posting.getIsTimeFlexible())
          .providesSupplyChain(posting.getProvidesSupplyChain())
          .specificTools(posting.getSpecificTools())
          .supplyChainItems(posting.getSupplyChainItems())
          .jobType(app.getJobRequirement().getJobType()) // Dynamic String
          .paymentType(app.getJobRequirement().getPaymentType().name())
          .payRate(app.getJobRequirement().getPayRate())
          .answers(app.getJobRequirement().getAnswers().stream().map(a -> new RequirementAnswerDto(a.getQuestion(), a.getAnswer())).toList())
          .applicationStatus(app.getStatus().name())
          .jobStatus(posting.getStatus().name())
          .reviewedBusiness(app.isReviewedBusiness())
          .build();
      })
      .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<BusinessDashboardResponse> getBusinessDashboard(String email) {
    User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    // FIX: Using findByUserId
    BusinessProfile business = businessProfileRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException("Profile not found"));

    // FIX: Using findByBusinessIdOrderByStartDatetimeDesc
    return jobPostingRepository.findByBusinessIdOrderByStartDatetimeDesc(business.getId()).stream()
      .map(posting -> {
        String fullAddress = posting.getAddress() + ", " + posting.getCity() + ", " + posting.getProvince() + " " + posting.getPostalCode();

        List<RequirementDetailDto> reqDetails = posting.getRequirements().stream()
          .map(req -> {
            List<AssignedWorkerDto> assigned = req.getAssignedWorkers().stream()
              .filter(app -> app.getStatus() == ApplicationStatus.SELECTED)
              .map(app -> AssignedWorkerDto.builder()
                .applicationId(app.getId())
                .fullName(app.getWorker().getFullName())
                .phoneNumber(app.getWorker().getPhoneNumber())
                .averageRating(app.getWorker().getAverageRating())
                .reviewedWorker(app.isReviewedWorker())
                .build())
              .collect(Collectors.toList());

            long pendingCount = req.getAssignedWorkers().stream()
              .filter(app -> app.getStatus() == ApplicationStatus.PENDING).count();

            return RequirementDetailDto.builder()
              .requirementId(req.getId())
              .jobType(req.getJobType()) // Dynamic String
              .paymentType(req.getPaymentType().name())
              .payRate(req.getPayRate())
              .qtyRequested(req.getQtyRequested())
              .qtyFilled(req.getQtyFilled())
              .pendingApplicantsCount(pendingCount)
              .answers(req.getAnswers().stream().map(a -> new RequirementAnswerDto(a.getQuestion(), a.getAnswer())).toList())
              .assignedWorkers(assigned)
              .build();
          }).collect(Collectors.toList());

        return BusinessDashboardResponse.builder()
          .jobPostingId(posting.getId())
          .address(fullAddress)
          .startDatetime(posting.getStartDatetime())
          .endDatetime(posting.getEndDatetime())
          .isTimeFlexible(posting.getIsTimeFlexible())
          .providesSupplyChain(posting.getProvidesSupplyChain())
          .specificTools(posting.getSpecificTools())
          .supplyChainItems(posting.getSupplyChainItems())
          .status(posting.getStatus().name())
          .requirements(reqDetails)
          .build();
      })
      .collect(Collectors.toList());
  }
}
