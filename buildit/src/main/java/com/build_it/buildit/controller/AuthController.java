package com.build_it.buildit.controller;

import com.build_it.buildit.dto.*;
import com.build_it.buildit.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register/worker")
  public ResponseEntity<String> registerWorker(@Valid @RequestBody WorkerRegisterRequest request) {
    return ResponseEntity.ok(authService.registerWorker(request));
  }

  @PostMapping("/register/business")
  public ResponseEntity<String> registerBusiness(@Valid @RequestBody BusinessRegisterRequest request) {
    return ResponseEntity.ok(authService.registerBusiness(request));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @GetMapping("/business/profile")
  public ResponseEntity<java.util.Map<String, String>> getBusinessProfile(java.security.Principal principal) {
    return ResponseEntity.ok(authService.getBusinessProfileContext(principal.getName()));
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<String> forgotPassword(@RequestParam String email) {
    return ResponseEntity.ok(authService.processForgotPassword(email));
  }

  @PostMapping("/reset-password")
  public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
    return ResponseEntity.ok(authService.resetPassword(token, newPassword));
  }

  @GetMapping("/verify-email")
  public ResponseEntity<String> verifyEmail(@RequestParam String token) {
    return ResponseEntity.ok(authService.verifyEmail(token));
  }
}
