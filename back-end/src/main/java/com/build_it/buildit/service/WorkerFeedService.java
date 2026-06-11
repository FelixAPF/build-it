package com.build_it.buildit.service;

import com.build_it.buildit.dto.AvailableJobResponse;
import com.build_it.buildit.dto.RequirementAnswerDto;
import com.build_it.buildit.entity.*;
import com.build_it.buildit.repository.JobApplicationRepository;
import com.build_it.buildit.repository.JobPostingRepository;
import com.build_it.buildit.repository.UserRepository;
import com.build_it.buildit.repository.WorkerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkerFeedService {

  private final JobPostingRepository jobPostingRepository;
  private final WorkerProfileRepository workerProfileRepository;
  private final JobApplicationRepository jobApplicationRepository;
  private final UserRepository userRepository;

  @Transactional
  public List<AvailableJobResponse> getFeedForWorker(String email) {
    User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    WorkerProfile worker = workerProfileRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException("Profile not found"));

    // Match against dynamic Strings rather than hardcoded Enums
    Set<String> workerSpecialties = worker.getSpecialties();
    LocalDateTime lastCheck = worker.getLastFeedCheck() != null ? worker.getLastFeedCheck() : LocalDateTime.now().minusDays(7);

    List<JobPosting> openPostings = jobPostingRepository.findByStatusIn(List.of(JobStatus.OPEN, JobStatus.PARTIALLY_FILLED));

    List<AvailableJobResponse> feed = openPostings.stream()
      .flatMap(posting -> posting.getRequirements().stream()
        .filter(req -> workerSpecialties.contains(req.getJobType())) // Use standard String matching
        .filter(req -> req.getQtyFilled() < req.getQtyRequested())
        .filter(req -> !jobApplicationRepository.existsByWorkerIdAndJobRequirementId(worker.getId(), req.getId()))
        .map(req -> {
          String fullAddress = posting.getAddress() + ", " + posting.getCity() + ", " + posting.getProvince() + " " + posting.getPostalCode();

          boolean isNew = posting.getCreatedAt() != null && posting.getCreatedAt().isAfter(lastCheck);

          return AvailableJobResponse.builder()
            .requirementId(req.getId())
            .jobPostingId(posting.getId())
            .companyName(posting.getBusiness().getCompanyName())
            .address(fullAddress)
            .startDatetime(posting.getStartDatetime())
            .endDatetime(posting.getEndDatetime())
            .isTimeFlexible(posting.getIsTimeFlexible())
            .providesSupplyChain(posting.getProvidesSupplyChain())
            .specificTools(posting.getSpecificTools())
            .supplyChainItems(posting.getSupplyChainItems())
            .jobType(req.getJobType()) // Dynamic String mapping
            .paymentType(req.getPaymentType())
            .payRate(req.getPayRate())
            .remainingSpots(req.getQtyRequested() - req.getQtyFilled())
            .isNewShift(isNew)
            .answers(req.getAnswers().stream().map(a -> new RequirementAnswerDto(a.getQuestion(), a.getAnswer())).toList())
            .build();
        })
      ).collect(Collectors.toList());

    // Update last check
    worker.setLastFeedCheck(LocalDateTime.now());
    workerProfileRepository.save(worker);

    return feed;
  }
}
