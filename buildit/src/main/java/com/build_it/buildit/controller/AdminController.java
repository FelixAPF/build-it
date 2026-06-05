package com.build_it.buildit.controller;

import com.build_it.buildit.dto.AuthResponse;
import com.build_it.buildit.dto.UserAdminResponse;
import com.build_it.buildit.entity.AuditLog;
import com.build_it.buildit.service.AdminService;
import com.build_it.buildit.service.AuditLogService;
import com.build_it.buildit.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

  private final AdminService adminService;
  private final AuditLogService auditLogService;
  private final FileStorageService fileStorageService;

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/users/{userId}/verify")
  public ResponseEntity<String> verifyUser(@PathVariable Long userId) {
    return ResponseEntity.ok(adminService.verifyUserAccount(userId));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/users/pending")
  public ResponseEntity<List<UserAdminResponse>> getPendingUsers() {
    return ResponseEntity.ok(adminService.getPendingUsers());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/users/all")
  public ResponseEntity<List<UserAdminResponse>> getAllUsers() {
    return ResponseEntity.ok(adminService.getAllUsers());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/users/{userId}/impersonate")
  public ResponseEntity<AuthResponse> impersonateUser(@PathVariable Long userId) {
    return ResponseEntity.ok(adminService.impersonateUser(userId));
  }

  // NEW AUDIT FETCH API ROUTE
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/logs")
  public ResponseEntity<List<AuditLog>> getAuditLogs() {
    return ResponseEntity.ok(auditLogService.getAllLogs());
  }

  // ... inside your AdminController class ...

  // NEW: Fetch logs for a specific user
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/users/{userId}/logs")
  public ResponseEntity<List<AuditLog>> getUserLogs(@PathVariable Long userId) {
    return ResponseEntity.ok(adminService.getUserLogs(userId));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/documents/{fileName:.+}")
  public ResponseEntity<Resource> getDocument(@PathVariable String fileName) {
    Resource resource = fileStorageService.loadFileAsResource(fileName);
    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
      .body(resource);
  }
}
