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

  @Transactional(readOnly = true)
  public List<WorkerDashboardResponse> getWorkerDashboard(String email) {
    User user = userRepository.findByEmail(email).orElseThrow();
    WorkerProfile worker = workerProfileRepository.findByUserId(user.getId()).orElseThrow();

    // Fetch ALL applications for this worker (Past, Present, Pending, Selected)
    List<JobApplication> applications = jobApplicationRepository.findByWorkerId(worker.getId());

    return applications.stream()
      .map(app -> {
        JobPosting posting = app.getJobRequirement().getJobPosting();
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
          .build();
      })
      // Sort so the newest/upcoming jobs are at the top
      .sorted((a, b) -> b.getStartDatetime().compareTo(a.getStartDatetime()))
      .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<BusinessDashboardResponse> getBusinessDashboard(String email) {
    User user = userRepository.findByEmail(email).orElseThrow();
    BusinessProfile business = businessProfileRepository.findByUserId(user.getId()).orElseThrow();

    // Fetch ALL jobs posted by this business ordered by start date (from Phase 1.5)
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
              // ONLY SHOW HIRED WORKERS IN THE ASSIGNED LIST
              .filter(app -> app.getStatus() == ApplicationStatus.SELECTED)
              .map(app -> AssignedWorkerDto.builder()
                .applicationId(app.getId())
                .workerId(app.getWorker().getId())
                .fullName(app.getWorker().getFullName())
                .phoneNumber(app.getWorker().getPhoneNumber())
                .averageRating(app.getWorker().getAverageRating())
                .build())
              .collect(Collectors.toList()))
            .build())
          .collect(Collectors.toList()))
        .build())
      .collect(Collectors.toList());
  }
}
