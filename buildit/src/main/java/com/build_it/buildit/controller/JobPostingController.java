package com.build_it.buildit.controller;

import com.build_it.buildit.dto.CreateJobPostingRequest;
import com.build_it.buildit.entity.JobPosting;
import com.build_it.buildit.service.JobPostingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobPostingController {

  private final JobPostingService jobPostingService;

  // Only allow verified BUSINESS accounts to hit this route
  @PreAuthorize("hasRole('BUSINESS')")
  @PostMapping
  public ResponseEntity<?> createJobPosting(
    @Valid @RequestBody CreateJobPostingRequest request,
    Principal principal) {

    // principal.getName() automatically contains the email parsed from the JWT Subject
    JobPosting createdJob = jobPostingService.createJobPosting(request, principal.getName());

    return ResponseEntity.ok("Job posted successfully. Total Fee Charged: $" + createdJob.getTotalAppFeeCharged());
  }

  @PreAuthorize("hasRole('BUSINESS')")
  @PutMapping("/{jobId}/cancel")
  public ResponseEntity<String> cancelJobPosting(@PathVariable Long jobId, Principal principal) {
    String response = jobPostingService.cancelJobPosting(jobId, principal.getName());
    return ResponseEntity.ok(response);
  }
}
