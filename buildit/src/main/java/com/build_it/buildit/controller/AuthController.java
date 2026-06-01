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
  public ResponseEntity<AuthResponse> registerWorker(@Valid @RequestBody WorkerRegisterRequest request) {
    return ResponseEntity.ok(authService.registerWorker(request));
  }

  @PostMapping("/register/business")
  public ResponseEntity<AuthResponse> registerBusiness(@Valid @RequestBody BusinessRegisterRequest request) {
    return ResponseEntity.ok(authService.registerBusiness(request));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }
}
