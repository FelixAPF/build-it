package com.build_it.buildit.service;

import com.build_it.buildit.dto.CreateReviewRequest;
import com.build_it.buildit.entity.*;
import com.build_it.buildit.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final JobApplicationRepository applicationRepository;
  private final JobPostingRepository jobPostingRepository;
  private final UserRepository userRepository;
  private final WorkerProfileRepository workerProfileRepository;
  private final BusinessProfileRepository businessProfileRepository;
  private final AuditLogService auditLogService;

  @Transactional
  public String reviewWorker(Long applicationId, CreateReviewRequest request, String businessEmail) {
    User businessUser = userRepository.findByEmail(businessEmail).orElseThrow();
    JobApplication application = applicationRepository.findById(applicationId)
      .orElseThrow(() -> new RuntimeException("Application not found"));

    JobPosting job = application.getJobRequirement().getJobPosting();
    User workerUser = application.getWorker().getUser();

    if (!job.getBusiness().getUser().getId().equals(businessUser.getId())) {
      throw new RuntimeException("You cannot review a worker for a job you do not own.");
    }

    // Security Check
    if (reviewRepository.existsByReviewerIdAndRevieweeIdAndJobPostingId(businessUser.getId(), workerUser.getId(), job.getId())) {
      throw new RuntimeException("You have already reviewed this worker for this job.");
    }

    Review review = new Review();
    review.setReviewer(businessUser);
    review.setReviewee(workerUser);
    review.setJobPosting(job); // <-- CRITICAL FIX: Explicitly tie the review to the job!
    review.setStarRating(request.getStarRating());
    review.setComment(request.getComment());
    review.setCreatedAt(LocalDateTime.now());
    reviewRepository.save(review);

    // Call your existing average update method here
    updateWorkerAverage(application.getWorker());
    auditLogService.log(businessEmail, "REVIEW_SUBMITTED", "Gave worker account " + workerUser.getEmail() + " a score of " + request.getStarRating() + " stars.");
    return "Review submitted successfully.";
  }

  @Transactional
  public String reviewBusiness(Long jobId, CreateReviewRequest request, String workerEmail) {
    User workerUser = userRepository.findByEmail(workerEmail).orElseThrow();
    WorkerProfile worker = workerProfileRepository.findByUserId(workerUser.getId()).orElseThrow();

    JobPosting job = jobPostingRepository.findById(jobId)
      .orElseThrow(() -> new RuntimeException("Job posting not found"));

    User businessUser = job.getBusiness().getUser();

    if (LocalDateTime.now().isBefore(job.getEndDatetime())) {
      throw new RuntimeException("You cannot leave a review before the job has ended.");
    }

    // Security Check
    if (reviewRepository.existsByReviewerIdAndRevieweeIdAndJobPostingId(workerUser.getId(), businessUser.getId(), job.getId())) {
      throw new RuntimeException("You have already reviewed this contractor for this job.");
    }

    Review review = new Review();
    review.setReviewer(workerUser);
    review.setReviewee(businessUser);
    review.setJobPosting(job); // <-- CRITICAL FIX: Explicitly tie the review to the job!
    review.setStarRating(request.getStarRating());
    review.setComment(request.getComment());
    review.setCreatedAt(LocalDateTime.now());
    reviewRepository.save(review);

    // Call your existing average update method here
    updateBusinessAverage(job.getBusiness());
    auditLogService.log(workerEmail, "REVIEW_SUBMITTED", "Gave company account " + businessUser.getEmail() + " a score of " + request.getStarRating() + " stars.");
    return "Review submitted successfully.";
  }

  // --- Helper Methods to Recalculate Averages ---

  private void updateWorkerAverage(WorkerProfile worker) {
    List<Review> reviews = reviewRepository.findByRevieweeId(worker.getUser().getId());
    double avg = reviews.stream().mapToInt(Review::getStarRating).average().orElse(0.0);
    // Round to 1 decimal place (e.g. 4.5)
    worker.setAverageRating(Math.round(avg * 10.0) / 10.0);
    workerProfileRepository.save(worker);
  }

  private void updateBusinessAverage(BusinessProfile business) {
    List<Review> reviews = reviewRepository.findByRevieweeId(business.getUser().getId());
    double avg = reviews.stream().mapToInt(Review::getStarRating).average().orElse(0.0);
    business.setAverageRating(Math.round(avg * 10.0) / 10.0);
    businessProfileRepository.save(business);
  }


}
