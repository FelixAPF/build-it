package com.build_it.buildit.controller;

import com.build_it.buildit.dto.AvailableJobResponse;
import com.build_it.buildit.service.WorkerFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/worker/feed")
@RequiredArgsConstructor
public class WorkerFeedController {

  private final WorkerFeedService workerFeedService;

  @PreAuthorize("hasRole('WORKER')")
  @GetMapping
  public ResponseEntity<List<AvailableJobResponse>> getMyFeed(Principal principal) {
    List<AvailableJobResponse> feed = workerFeedService.getFeedForWorker(principal.getName());
    return ResponseEntity.ok(feed);
  }
}
