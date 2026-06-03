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
  private final ReviewRepository reviewRepository; // <-- 1. INJECT REPOSITORY

  @Transactional(readOnly = true)
  public List<WorkerDashboardResponse> getWorkerDashboard(String email) {
    User user = userRepository.findByEmail(email).orElseThrow();
    WorkerProfile worker = workerProfileRepository.findByUserId(user.getId()).orElseThrow();

    List<JobApplication> applications = jobApplicationRepository.findByWorkerId(worker.getId());

    return applications.stream()
      .map(app -> {
        JobPosting posting = app.getJobRequirement().getJobPosting();

        // 2. EVALUATE WORKER TO BUSINESS ACTION
        boolean alreadyReviewed = reviewRepository.existsByReviewerIdAndRevieweeIdAndJobPostingId(
          user.getId(), posting.getBusiness().getUser().getId(), posting.getId()
        );

        return WorkerDashboardResponse.builder()
          .applicationId(app.getId())
          .jobId(posting.getId())
          .companyName(posting.getBusiness().getCompanyName())
          .companyPhone(posting.getBusiness().getPhoneNumber())
          .address(posting.getAddress())
          .startDatetime(posting.getStartDatetime())
          .endDatetime(posting.getEndDatetime())
          .jobType(app.getJobRequirement().getJobType().name())
          .hourlyRate(app.getJobRequirement().getHourlyRate())
          .applicationStatus(app.getStatus().name())
          .jobStatus(posting.getStatus().name())
          .reviewedBusiness(alreadyReviewed) // <-- MAP IT HERE
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
      .map(posting -> BusinessDashboardResponse.builder()
        .jobPostingId(posting.getId())
        .address(posting.getAddress())
        .startDatetime(posting.getStartDatetime())
        .endDatetime(posting.getEndDatetime())
        .status(posting.getStatus().name())
        .requirements(posting.getRequirements().stream()
          .map(req -> RequirementDetailDto.builder()
            .requirementId(req.getId())
            .jobType(req.getJobType().name())
            .hourlyRate(req.getHourlyRate())
            .qtyRequested(req.getQtyRequested())
            .qtyFilled(req.getQtyFilled())
            .assignedWorkers(req.getApplications().stream()
              .filter(app -> app.getStatus() == ApplicationStatus.SELECTED)
              .map(app -> {
                // 3. EVALUATE BUSINESS TO WORKER ACTION
                boolean alreadyReviewed = reviewRepository.existsByReviewerIdAndRevieweeIdAndJobPostingId(
                  user.getId(), app.getWorker().getUser().getId(), posting.getId()
                );

                return AssignedWorkerDto.builder()
                  .applicationId(app.getId())
                  .workerId(app.getWorker().getId())
                  .fullName(app.getWorker().getFullName())
                  .phoneNumber(app.getWorker().getPhoneNumber())
                  .averageRating(app.getWorker().getAverageRating())
                  .reviewedWorker(alreadyReviewed) // <-- MAP IT HERE
                  .build();
              })
              .collect(Collectors.toList()))
            .build())
          .collect(Collectors.toList()))
        .build())
      .collect(Collectors.toList());
  }
}
