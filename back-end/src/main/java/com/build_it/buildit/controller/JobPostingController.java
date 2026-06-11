package com.build_it.buildit.controller;

import com.build_it.buildit.dto.CreateJobPostingRequest;
import com.build_it.buildit.service.JobPostingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map; // <-- Add this

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobPostingController {

  private final JobPostingService jobPostingService;

  @PreAuthorize("hasRole('BUSINESS')")
  @PostMapping
  public ResponseEntity<?> createJobPosting(@Valid @RequestBody CreateJobPostingRequest request, Principal principal) {
    // Returns the Stripe Checkout URL
    String checkoutUrl = jobPostingService.createJobPosting(request, principal.getName());

    // Send it back as a clean JSON object
    return ResponseEntity.ok(Map.of("checkoutUrl", checkoutUrl));
  }

  @PreAuthorize("hasRole('BUSINESS')")
  @PutMapping("/{jobId}/cancel")
  public ResponseEntity<String> cancelJobPosting(@PathVariable Long jobId, Principal principal) {
    String response = jobPostingService.cancelJobPosting(jobId, principal.getName());
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasRole('BUSINESS')")
  @PostMapping("/{jobId}/pay")
  public ResponseEntity<?> payForExistingJob(@PathVariable Long jobId, Principal principal) {
    String checkoutUrl = jobPostingService.generatePaymentSessionForExistingJob(jobId, principal.getName());
    // Return the new Stripe URL back to Angular
    return ResponseEntity.ok(java.util.Map.of("checkoutUrl", checkoutUrl));
  }
}
