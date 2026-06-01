package com.build_it.buildit.controller;

import com.build_it.buildit.dto.BusinessDashboardResponse;
import com.build_it.buildit.dto.WorkerDashboardResponse;
import com.build_it.buildit.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

  private final DashboardService dashboardService;

  // WORKER VIEW: All their shifts (Pending, Upcoming, Past)
  @PreAuthorize("hasRole('WORKER')")
  @GetMapping("/worker")
  public ResponseEntity<List<WorkerDashboardResponse>> getWorkerDashboard(Principal principal) {
    return ResponseEntity.ok(dashboardService.getWorkerDashboard(principal.getName()));
  }

  // BUSINESS VIEW: All their postings and the assigned workers
  @PreAuthorize("hasRole('BUSINESS')")
  @GetMapping("/business")
  public ResponseEntity<List<BusinessDashboardResponse>> getBusinessDashboard(Principal principal) {
    return ResponseEntity.ok(dashboardService.getBusinessDashboard(principal.getName()));
  }
}
