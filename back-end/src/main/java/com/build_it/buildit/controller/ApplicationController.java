package com.build_it.buildit.controller;

import com.build_it.buildit.dto.ApplicationResponse;
import com.build_it.buildit.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

  private final ApplicationService applicationService;

  // WORKER ENDPOINT
  @PreAuthorize("hasRole('WORKER')")
  @PostMapping("/worker/{requirementId}/apply")
  public ResponseEntity<String> applyForJob(@PathVariable Long requirementId, Principal principal) {
    String response = applicationService.applyForJob(requirementId, principal.getName());
    return ResponseEntity.ok(response);
  }

  // BUSINESS ENDPOINT
  @PreAuthorize("hasRole('BUSINESS')")
  @PostMapping("/business/{applicationId}/approve")
  public ResponseEntity<String> approveWorker(@PathVariable Long applicationId, Principal principal) {
    String response = applicationService.approveApplication(applicationId, principal.getName());
    return ResponseEntity.ok(response);
  }

  // BUSINESS ENDPOINT: View Applicants
  @PreAuthorize("hasRole('BUSINESS')")
  @GetMapping("/business/requirements/{requirementId}/applications")
  public ResponseEntity<List<ApplicationResponse>> getPendingApplications(
    @PathVariable Long requirementId,
    Principal principal) {

    List<ApplicationResponse> applicants = applicationService
      .getPendingApplicationsForRequirement(requirementId, principal.getName());

    return ResponseEntity.ok(applicants);
  }

  @PreAuthorize("hasRole('BUSINESS')")
  @PostMapping("/business/{applicationId}/reject")
  public ResponseEntity<String> rejectWorker(@PathVariable Long applicationId, Principal principal) {
    String response = applicationService.rejectApplication(applicationId, principal.getName());
    return ResponseEntity.ok(response);
  }
}
