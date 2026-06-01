package com.build_it.buildit.controller;

import com.build_it.buildit.dto.CreateReviewRequest;
import com.build_it.buildit.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;

  // BUSINESS ENDPOINT: Review a Worker using the Application ID
  @PreAuthorize("hasRole('BUSINESS')")
  @PostMapping("/worker/{applicationId}")
  public ResponseEntity<String> reviewWorker(
    @PathVariable Long applicationId,
    @Valid @RequestBody CreateReviewRequest request,
    Principal principal) {

    String response = reviewService.reviewWorker(applicationId, request, principal.getName());
    return ResponseEntity.ok(response);
  }

  // WORKER ENDPOINT: Review a Business using the Job ID
  @PreAuthorize("hasRole('WORKER')")
  @PostMapping("/business/{jobId}")
  public ResponseEntity<String> reviewBusiness(
    @PathVariable Long jobId,
    @Valid @RequestBody CreateReviewRequest request,
    Principal principal) {

    String response = reviewService.reviewBusiness(jobId, request, principal.getName());
    return ResponseEntity.ok(response);
  }
}
