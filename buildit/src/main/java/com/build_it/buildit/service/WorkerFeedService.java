package com.build_it.buildit.service;

import com.build_it.buildit.dto.AvailableJobResponse;
import com.build_it.buildit.dto.RequirementAnswerDto;
import com.build_it.buildit.entity.*;
import com.build_it.buildit.repository.JobApplicationRepository;
import com.build_it.buildit.repository.JobRequirementRepository;
import com.build_it.buildit.repository.UserRepository;
import com.build_it.buildit.repository.WorkerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkerFeedService {

  private final UserRepository userRepository;
  private final WorkerProfileRepository workerProfileRepository;
  private final JobRequirementRepository jobRequirementRepository;
  private final JobApplicationRepository jobApplicationRepository;

  @Transactional
  public List<AvailableJobResponse> getFeedForWorker(String email) {

    User user = userRepository.findByEmail(email).orElseThrow();
    WorkerProfile worker = workerProfileRepository.findByUserId(user.getId()).orElseThrow();

    if (user.getStatus() != AccountStatus.ACTIVE) {
      throw new RuntimeException("Account pending verification.");
    }

    LocalDateTime lastCheck = worker.getLastFeedCheck();

    List<JobApplication> confirmedShifts = jobApplicationRepository
      .findByWorkerIdAndStatus(worker.getId(), ApplicationStatus.SELECTED);

    List<JobApplication> allWorkerApps = jobApplicationRepository.findByWorkerId(worker.getId());
    java.util.Set<Long> appliedReqIds = allWorkerApps.stream()
      .map(app -> app.getJobRequirement().getId())
      .collect(java.util.stream.Collectors.toSet());

    List<JobRequirement> potentialMatches = jobRequirementRepository.findAvailableRequirements(
      worker.getSpecialties(),
      List.of(JobStatus.OPEN, JobStatus.PARTIALLY_FILLED)
    );

    List<AvailableJobResponse> response = potentialMatches.stream()
      .filter(req -> !appliedReqIds.contains(req.getId()))
      .filter(req -> !isOverlapping(req.getJobPosting(), confirmedShifts))
      .map(req -> {

        boolean isNew = (lastCheck == null || req.getJobPosting().getCreatedAt().isAfter(lastCheck));

        String fullAddress = req.getJobPosting().getAddress();
        if (req.getJobPosting().getCity() != null) {
          fullAddress += ", " + req.getJobPosting().getCity() + ", " + req.getJobPosting().getProvince() + " " + req.getJobPosting().getPostalCode();
        }

        return AvailableJobResponse.builder()
          .requirementId(req.getId())
          .jobPostingId(req.getJobPosting().getId())
          .companyName(req.getJobPosting().getBusiness().getCompanyName())
          .address(fullAddress)
          .startDatetime(req.getJobPosting().getStartDatetime())
          .endDatetime(req.getJobPosting().getEndDatetime())
          .isTimeFlexible(req.getJobPosting().getIsTimeFlexible())
          .jobType(req.getJobType())
          .paymentType(req.getPaymentType()) // <-- NEW
          .providesSupplyChain(req.getJobPosting().getProvidesSupplyChain()) // <-- NEW
          .specificTools(req.getJobPosting().getSpecificTools()) // <-- NEW
          .answers(req.getAnswers().stream().map(a -> new RequirementAnswerDto(a.getQuestion(), a.getAnswer())).toList()) // <-- NEW
          .payRate(req.getPayRate())
          .remainingSpots(req.getQtyRequested() - req.getQtyFilled())
          .isNewShift(isNew)
          .build();
      })
      .collect(java.util.stream.Collectors.toList());

    worker.setLastFeedCheck(LocalDateTime.now());
    workerProfileRepository.save(worker);

    return response;
  }

  private boolean isOverlapping(JobPosting potentialJob, List<JobApplication> confirmedShifts) {
    for (JobApplication shift : confirmedShifts) {
      JobPosting confirmedJob = shift.getJobRequirement().getJobPosting();

      if (potentialJob.getStartDatetime().isBefore(confirmedJob.getEndDatetime()) &&
        potentialJob.getEndDatetime().isAfter(confirmedJob.getStartDatetime())) {
        return true;
      }
    }
    return false;
  }
}
