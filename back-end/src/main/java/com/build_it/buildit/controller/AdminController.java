package com.build_it.buildit.controller;

import com.build_it.buildit.dto.AuthResponse;
import com.build_it.buildit.dto.UserAdminResponse;
import com.build_it.buildit.entity.AuditLog;
import com.build_it.buildit.entity.Trade;
import com.build_it.buildit.entity.TradeQuestion;
import com.build_it.buildit.service.AdminService;
import com.build_it.buildit.service.AuditLogService;
import com.build_it.buildit.service.FileStorageService;
import com.build_it.buildit.repository.TradeRepository;
import com.build_it.buildit.repository.TradeQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
  private final TradeRepository tradeRepository;
  private final TradeQuestionRepository tradeQuestionRepository;

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/users/{userId}/verify")
  public ResponseEntity<?> verifyUser(@PathVariable Long userId) {
    // FIX: Using your correct custom method name
    String responseMessage = adminService.verifyUserAccount(userId);
    return ResponseEntity.ok(java.util.Map.of("message", responseMessage));
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

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/metrics")
  public ResponseEntity<java.util.Map<String, Long>> getMetrics() {
    return ResponseEntity.ok(adminService.getSystemMetrics());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/logs")
  public ResponseEntity<List<AuditLog>> getAuditLogs() {
    return ResponseEntity.ok(auditLogService.getAllLogs());
  }
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/users/{userId}/suspend")
  public ResponseEntity<?> suspendUser(@PathVariable Long userId) {
    String responseMessage = adminService.suspendUserAccount(userId);
    return ResponseEntity.ok(java.util.Map.of("message", responseMessage));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/users/{userId}/logs")
  public ResponseEntity<List<AuditLog>> getUserLogs(@PathVariable Long userId) {
    return ResponseEntity.ok(adminService.getUserLogs(userId));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/documents/{fileName:.+}")
  public ResponseEntity<Resource> getDocument(@PathVariable String fileName) {
    // FIX: Using your correct FileStorageService
    Resource resource = fileStorageService.loadFileAsResource(fileName);
    String contentType = fileName.toLowerCase().endsWith(".pdf") ? "application/pdf" : "image/jpeg";

    return ResponseEntity.ok()
      .contentType(MediaType.parseMediaType(contentType))
      .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
      .body(resource);
  }


  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/trade-questions")
  public TradeQuestion addTradeQuestion(@RequestBody TradeQuestion question) {
    return tradeQuestionRepository.save(question);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/trade-questions/{id}")
  public void deleteTradeQuestion(@PathVariable Long id) {
    tradeQuestionRepository.deleteById(id);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/trades")
  public Trade addTrade(@RequestBody Trade trade) {
    return tradeRepository.save(trade);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/trades/{id}")
  public void deleteTrade(@PathVariable Long id) {
    tradeRepository.deleteById(id);
  }
}
