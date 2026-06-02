package com.build_it.buildit.controller;

import com.build_it.buildit.dto.UserAdminResponse;
import com.build_it.buildit.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

  private final AdminService adminService;

  // Only allow users with the ADMIN role to access this endpoint
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/users/{userId}/verify")
  public ResponseEntity<String> verifyUser(@PathVariable Long userId) {
    String responseMessage = adminService.verifyUserAccount(userId);
    return ResponseEntity.ok(responseMessage);
  }

  // ADD THIS BELOW YOUR EXISTING PUT MAPPING
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/users/pending")
  public ResponseEntity<List<UserAdminResponse>> getPendingUsers() {
    return ResponseEntity.ok(adminService.getPendingUsers());
  }
}
