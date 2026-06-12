package com.build_it.buildit.controller;

import com.build_it.buildit.dto.CreateJobPostingRequest;
import com.build_it.buildit.service.JobPostingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

  @Value("app.frontend-url")
  private String frontendUrl;

  @PreAuthorize("hasRole('BUSINESS')")
  @PostMapping
  public ResponseEntity<Map<String, String>> createJobPosting(
          @RequestBody CreateJobPostingRequest request,
          Principal principal,
          @RequestParam(value = "frontendUrl", required = false) String sentFrontendUrl // <-- 1. Accept the parameter
  ) {
    // 2. Fallback to localhost if Angular hasn't sent the parameter yet
    String dynamicFrontendUrl = (frontendUrl != null && !frontendUrl.isBlank())
            ? sentFrontendUrl
            : frontendUrl;

    // 3. Pass all 3 arguments into the service layer
    String checkoutUrl = jobPostingService.createJobPosting(request, principal.getName(), dynamicFrontendUrl);

    return ResponseEntity.ok(Map.of("stripeSessionUrl", checkoutUrl));
  }

  @PreAuthorize("hasRole('BUSINESS')")
  @PutMapping("/{jobId}/cancel")
  public ResponseEntity<String> cancelJobPosting(@PathVariable Long jobId, Principal principal) {
    String response = jobPostingService.cancelJobPosting(jobId, principal.getName());
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasRole('BUSINESS')")
  @PostMapping("/{jobId}/pay")
  public ResponseEntity<?> payForExistingJob(@PathVariable Long jobId, Principal principal,
                                             @RequestParam(value = "frontendUrl", required = false) String sentFrontendUrl) {
    String dynamicFrontendUrl = (frontendUrl != null && !frontendUrl.isBlank())
            ? sentFrontendUrl
            : frontendUrl;
    String checkoutUrl = jobPostingService.generatePaymentSessionForExistingJob(jobId, principal.getName(), dynamicFrontendUrl);
    // Return the new Stripe URL back to Angular
    return ResponseEntity.ok(java.util.Map.of("checkoutUrl", checkoutUrl));
  }
}
