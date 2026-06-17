package com.build_it.buildit.controller;

import com.build_it.buildit.service.AuthService;
import com.build_it.buildit.service.FileStorageService; // <-- NEW IMPORT
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final AuthService authService;
  private final FileStorageService fileStorageService; // <-- INJECTED SERVICE


  @PreAuthorize("isAuthenticated()")
  @PostMapping("/upload-documents")
  public ResponseEntity<String> uploadDocuments(@RequestParam("file") MultipartFile file, Principal principal) {
    String savedFileName = fileStorageService.storeFile(file, principal.getName());
    // Pass the saved filename to the auth service
    return ResponseEntity.ok(authService.completeDocumentUpload(principal.getName(), savedFileName));
  }

  @PreAuthorize("isAuthenticated()")
  @PostMapping("/device-token")
  public ResponseEntity<?> updateDeviceToken(@RequestBody Map<String, String> payload, Principal principal) {
    String token = payload.get("token");
    if (token != null) {
      authService.updateDeviceToken(principal.getName(), token);
    }
    return ResponseEntity.ok().body("Device token updated");
  }
}
