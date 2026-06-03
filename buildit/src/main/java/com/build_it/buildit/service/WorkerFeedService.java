package com.build_it.buildit.service;

import com.build_it.buildit.dto.AvailableJobResponse;
import com.build_it.buildit.entity.*;
import com.build_it.buildit.repository.JobApplicationRepository;
import com.build_it.buildit.repository.JobRequirementRepository;
import com.build_it.buildit.repository.UserRepository;
import com.build_it.buildit.repository.WorkerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkerFeedService {

  private final UserRepository userRepository;
  private final WorkerProfileRepository workerProfileRepository;
  private final JobRequirementRepository jobRequirementRepository;
  private final JobApplicationRepository jobApplicationRepository;

  @Transactional(readOnly = true)
  public List<AvailableJobResponse> getFeedForWorker(String email) {

    // 1. Get the Worker
    User user = userRepository.findByEmail(email).orElseThrow();
    WorkerProfile worker = workerProfileRepository.findByUserId(user.getId()).orElseThrow();

    if (user.getStatus() != AccountStatus.ACTIVE) {
      throw new RuntimeException("Account pending verification.");
    }

    // 2. Get Worker's currently confirmed schedule
    List<JobApplication> confirmedShifts = jobApplicationRepository
      .findByWorkerIdAndStatus(worker.getId(), ApplicationStatus.SELECTED);

    // NEW: Get ALL of the worker's applications (Pending, Rejected, Selected, Cancelled)
    List<JobApplication> allWorkerApps = jobApplicationRepository.findByWorkerId(worker.getId());
    java.util.Set<Long> appliedReqIds = allWorkerApps.stream()
      .map(app -> app.getJobRequirement().getId())
      .collect(java.util.stream.Collectors.toSet());

    // 3. Fetch all open requirements matching their trade
    List<JobRequirement> potentialMatches = jobRequirementRepository.findAvailableRequirements(
      worker.getSpecialties(),
      List.of(JobStatus.OPEN, JobStatus.PARTIALLY_FILLED)
    );

    // 4. Filter out already applied jobs AND overlapping shifts
    return potentialMatches.stream()
      .filter(req -> !appliedReqIds.contains(req.getId())) // <-- Hides applied/rejected jobs!
      .filter(req -> !isOverlapping(req.getJobPosting(), confirmedShifts))
      .map(req -> AvailableJobResponse.builder()
        .requirementId(req.getId())
        .jobPostingId(req.getJobPosting().getId())
        .companyName(req.getJobPosting().getBusiness().getCompanyName())
        .address(req.getJobPosting().getAddress())
        .startDatetime(req.getJobPosting().getStartDatetime())
        .endDatetime(req.getJobPosting().getEndDatetime())
        .jobType(req.getJobType())
        .hourlyRate(req.getHourlyRate())
        .remainingSpots(req.getQtyRequested() - req.getQtyFilled())
        .build())
      .collect(java.util.stream.Collectors.toList());
  }

  // Helper method to check time overlaps
  private boolean isOverlapping(JobPosting potentialJob, List<JobApplication> confirmedShifts) {
    for (JobApplication shift : confirmedShifts) {
      JobPosting confirmedJob = shift.getJobRequirement().getJobPosting();

      // Overlap formula: (StartA < EndB) AND (EndA > StartB)
      if (potentialJob.getStartDatetime().isBefore(confirmedJob.getEndDatetime()) &&
        potentialJob.getEndDatetime().isAfter(confirmedJob.getStartDatetime())) {
        return true; // Conflict found!
      }
    }
    return false; // No conflict
  }
}
