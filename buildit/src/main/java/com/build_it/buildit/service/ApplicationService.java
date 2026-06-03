package com.build_it.buildit.service;

import com.build_it.buildit.dto.ApplicationResponse;
import com.build_it.buildit.entity.*;
import com.build_it.buildit.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

  private final JobApplicationRepository applicationRepository;
  private final JobRequirementRepository requirementRepository;
  private final JobPostingRepository jobPostingRepository;
  private final WorkerProfileRepository workerProfileRepository;
  private final BusinessProfileRepository businessProfileRepository;
  private final UserRepository userRepository;
  private final AuditLogService auditLogService;
  private final EmailService emailService;

  // STEP 3.3: Worker Applies
  @Transactional
  public String applyForJob(Long requirementId, String workerEmail) {
    User user = userRepository.findByEmail(workerEmail).orElseThrow();
    WorkerProfile worker = workerProfileRepository.findByUserId(user.getId()).orElseThrow();

    if (user.getStatus() != AccountStatus.ACTIVE) {
      throw new RuntimeException("Account pending verification.");
    }

    JobRequirement req = requirementRepository.findById(requirementId)
      .orElseThrow(() -> new RuntimeException("Job requirement not found"));

    if (req.getQtyFilled() >= req.getQtyRequested()) {
      throw new RuntimeException("This position is already fully staffed.");
    }

    if (applicationRepository.existsByWorkerIdAndJobRequirementId(worker.getId(), req.getId())) {
      throw new RuntimeException("You have already applied for this position.");
    }

    JobApplication application = JobApplication.builder()
      .jobRequirement(req)
      .worker(worker)
      .status(ApplicationStatus.PENDING)
      .build();

    applicationRepository.save(application);
    auditLogService.log(workerEmail, "JOB_APPLICATION", "Submitted availability match application tracking for slot ID: " + requirementId);
    return "Successfully applied for the position!";
  }
  @Transactional
  public String approveApplication(Long applicationId, String businessEmail) {
    // ... (Keep the existing security checks and approval logic exactly the same) ...

    User user = userRepository.findByEmail(businessEmail).orElseThrow();
    BusinessProfile business = businessProfileRepository.findByUserId(user.getId()).orElseThrow();

    if (user.getStatus() != AccountStatus.ACTIVE) {
      throw new RuntimeException("Account pending verification.");
    }

    JobApplication application = applicationRepository.findById(applicationId)
      .orElseThrow(() -> new RuntimeException("Application not found"));

    JobRequirement requirement = application.getJobRequirement();
    JobPosting posting = requirement.getJobPosting();

    // Security check: Make sure this business actually owns this job posting
    if (!posting.getBusiness().getId().equals(business.getId())) {
      throw new RuntimeException("You do not have permission to approve this application.");
    }

    if (application.getStatus() != ApplicationStatus.PENDING) {
      throw new RuntimeException("Application is no longer pending.");
    }

    if (requirement.getQtyFilled() >= requirement.getQtyRequested()) {
      throw new RuntimeException("This requirement is already full.");
    }

    // 1. Approve the application
    application.setStatus(ApplicationStatus.SELECTED);
    applicationRepository.save(application);

    // 2. Increment the filled quantity
    requirement.setQtyFilled(requirement.getQtyFilled() + 1);
    requirementRepository.save(requirement);

    // 3. Update parent Job Posting status if necessary
    boolean isFullyStaffed = posting.getRequirements().stream()
      .allMatch(r -> r.getQtyFilled() >= r.getQtyRequested());

    if (isFullyStaffed) {
      posting.setStatus(JobStatus.FULLY_FILLED);
      jobPostingRepository.save(posting);
    } else {
      posting.setStatus(JobStatus.PARTIALLY_FILLED);
      jobPostingRepository.save(posting);
    }

    // 4. THE HANDSHAKE MAGIC: Auto-cancel overlapping pending applications for the WINNING worker
    cancelOverlappingApplications(application.getWorker(), posting);

    if (requirement.getQtyFilled().equals(requirement.getQtyRequested())) {
      List<JobApplication> remainingApplicants = applicationRepository
        .findByJobRequirementIdAndStatus(requirement.getId(), ApplicationStatus.PENDING);

      for (JobApplication remainingApp : remainingApplicants) {
        remainingApp.setStatus(ApplicationStatus.REJECTED);
        applicationRepository.save(remainingApp);
      }
    }

    // ==========================================
    // 6. FIRE THE EMAIL TO THE WORKER!
    // ==========================================
    emailService.sendWorkerHiredEmail(
      application.getWorker().getUser().getEmail(),
      application.getWorker().getFullName(),
      business.getCompanyName(),
      posting.getAddress(),
      posting.getStartDatetime().toString()
    );

    auditLogService.log(businessEmail, "APPLICATION_APPROVED", "Hired tradesperson " + application.getWorker().getFullName() + " for requirement context.");
    return "Worker approved! Schedule locked.";
  }

  // NEW METHOD: Allow Business to manually reject an applicant
  @Transactional
  public String rejectApplication(Long applicationId, String businessEmail) {
    User user = userRepository.findByEmail(businessEmail).orElseThrow();
    BusinessProfile business = businessProfileRepository.findByUserId(user.getId()).orElseThrow();

    if (user.getStatus() != AccountStatus.ACTIVE) {
      throw new RuntimeException("Account pending verification.");
    }

    JobApplication application = applicationRepository.findById(applicationId)
      .orElseThrow(() -> new RuntimeException("Application not found"));

    if (!application.getJobRequirement().getJobPosting().getBusiness().getId().equals(business.getId())) {
      throw new RuntimeException("You do not have permission to reject this application.");
    }

    if (application.getStatus() != ApplicationStatus.PENDING) {
      throw new RuntimeException("You can only reject pending applications.");
    }

    application.setStatus(ApplicationStatus.REJECTED);
    applicationRepository.save(application);
    auditLogService.log(businessEmail, "APPLICATION_REJECTED", "Declined applicant portfolio matching sequence ID: " + applicationId);
    return "Application has been rejected.";
  }

  @Transactional(readOnly = true)
  public List<ApplicationResponse> getPendingApplicationsForRequirement(Long requirementId, String businessEmail) {
    User user = userRepository.findByEmail(businessEmail).orElseThrow();
    BusinessProfile business = businessProfileRepository.findByUserId(user.getId()).orElseThrow();

    JobRequirement requirement = requirementRepository.findById(requirementId)
      .orElseThrow(() -> new RuntimeException("Job requirement not found"));

    // Security check: Only the owner of the job can see the applicants
    if (!requirement.getJobPosting().getBusiness().getId().equals(business.getId())) {
      throw new RuntimeException("You do not have permission to view these applications.");
    }

    // Fetch all PENDING applications for this specific slot
    List<JobApplication> applications = applicationRepository
      .findByJobRequirementIdAndStatus(requirementId, ApplicationStatus.PENDING);

    // Map the database entities to our safe DTO
    return applications.stream()
      .map(app -> ApplicationResponse.builder()
        .applicationId(app.getId())
        .workerId(app.getWorker().getId())
        .workerName(app.getWorker().getFullName())
        .yearsExperience(app.getWorker().getYearsExperience())
        .averageRating(app.getWorker().getAverageRating())
        .appliedAt(app.getAppliedAt())
        .status(app.getStatus().name())
        .build())
      .toList();
  }

  private void cancelOverlappingApplications(WorkerProfile worker, JobPosting confirmedJob) {
    List<JobApplication> pendingApps = applicationRepository
      .findByWorkerIdAndStatus(worker.getId(), ApplicationStatus.PENDING);

    for (JobApplication pendingApp : pendingApps) {
      JobPosting pendingJob = pendingApp.getJobRequirement().getJobPosting();

      // Check for time overlap
      if (pendingJob.getStartDatetime().isBefore(confirmedJob.getEndDatetime()) &&
        pendingJob.getEndDatetime().isAfter(confirmedJob.getStartDatetime())) {

        pendingApp.setStatus(ApplicationStatus.AUTO_CANCELLED);
        applicationRepository.save(pendingApp);
      }
    }
  }
}
