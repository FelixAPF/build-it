package com.build_it.buildit.controller;

import com.build_it.buildit.entity.JobPosting;
import com.build_it.buildit.entity.JobStatus;
import com.build_it.buildit.repository.JobPostingRepository;
import com.build_it.buildit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final JobPostingRepository jobPostingRepository;
  private final AuditLogService auditLogService;

  @Transactional
  @PreAuthorize("hasRole('BUSINESS')")
  @PostMapping("/success/{jobId}")
  public ResponseEntity<?> confirmPayment(@PathVariable Long jobId, Principal principal) {
    JobPosting job = jobPostingRepository.findById(jobId)
      .orElseThrow(() -> new RuntimeException("Job not found"));

    // Ensure the person confirming it is the business owner
    if (!job.getBusiness().getUser().getEmail().equals(principal.getName())) {
      throw new RuntimeException("Unauthorized");
    }

    if (job.getStatus() == JobStatus.PENDING_PAYMENT) {
      job.setStatus(JobStatus.OPEN);
      jobPostingRepository.save(job);
      auditLogService.log(principal.getName(), "PAYMENT_SUCCESS", "Successfully paid app fee and opened job ID: " + jobId);
    }

    return ResponseEntity.ok(Map.of("message", "Payment confirmed and job is now live!"));
  }
}
