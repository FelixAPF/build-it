package com.build_it.buildit.service;

import com.build_it.buildit.dto.AuthResponse;
import com.build_it.buildit.dto.UserAdminResponse;
import com.build_it.buildit.entity.*;
import com.build_it.buildit.repository.*;
import com.build_it.buildit.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

  private final UserRepository userRepository;
  private final WorkerProfileRepository workerProfileRepository;
  private final BusinessProfileRepository businessProfileRepository;
  private final JwtUtils jwtUtils;
  private final AuditLogService auditLogService; // <-- Inject Service
  private final JobPostingRepository jobPostingRepository;
  private final JobApplicationRepository applicationRepository;

  @Transactional(readOnly = true)
  public List<UserAdminResponse> getPendingUsers() {
    return userRepository.findByStatus(AccountStatus.PENDING_VERIFICATION)
      .stream().map(this::mapToAdminResponse).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<UserAdminResponse> getAllUsers() {
    return userRepository.findAll()
      .stream()
      .filter(u -> u.getRole() != Role.ADMIN)
      .map(this::mapToAdminResponse).collect(Collectors.toList());
  }

  @Transactional
  public AuthResponse impersonateUser(Long userId) {
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new RuntimeException("User not found"));

    // Write entry to Audit Logs
    auditLogService.log("ADMINISTRATOR", "IMPERSONATION_START",
      "Began impersonating target session matching account identifier: " + user.getEmail());

    String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
    return new AuthResponse(token, user.getEmail(), user.getRole().name(), user.getStatus().name());
  }
  @Transactional(readOnly = true)
  public Map<String, Long> getSystemMetrics() {
    Map<String, Long> metrics = new HashMap<>();
    metrics.put("totalBusinesses", userRepository.countByRole(Role.BUSINESS));
    metrics.put("totalWorkers", userRepository.countByRole(Role.WORKER));
    metrics.put("totalJobs", jobPostingRepository.count());
    metrics.put("totalMatches", applicationRepository.countByStatus(ApplicationStatus.SELECTED));
    metrics.put("totalCancelled", jobPostingRepository.countByStatus(JobStatus.CANCELLED));
    return metrics;
  }

  @Transactional(readOnly = true)
  public List<com.build_it.buildit.entity.AuditLog> getUserLogs(Long userId) {
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new RuntimeException("User not found"));
    return auditLogService.getLogsByActor(user.getEmail());
  }

  @Transactional
  public String verifyUserAccount(Long userId) {
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

    if (user.getStatus() == AccountStatus.ACTIVE) return "User is already ACTIVE.";

    user.setStatus(AccountStatus.ACTIVE);
    userRepository.save(user);

    auditLogService.log("ADMINISTRATOR", "USER_ACTIVATION",
      "Verified credentials and activated user profile: " + user.getEmail());

    return "User account for " + user.getEmail() + " has been successfully verified.";
  }

  private UserAdminResponse mapToAdminResponse(User user) {
    String name = "Unknown";
    String idNumber = "Unknown";

    if (user.getRole() == Role.WORKER) {
      var worker = workerProfileRepository.findByUserId(user.getId()).orElse(null);
      if (worker != null) {
        name = worker.getFullName();
        idNumber = worker.getCcqNumber();
      }
    } else if (user.getRole() == Role.BUSINESS) {
      var business = businessProfileRepository.findByUserId(user.getId()).orElse(null);
      if (business != null) {
        name = business.getCompanyName();
        idNumber = business.getRbqNumber() != null ? business.getRbqNumber() : "N/A (Private)";
      }
    }

    String docUrl = user.getDocumentPath() != null ? "/api/admin/documents/" + user.getDocumentPath() : null;

    return UserAdminResponse.builder()
      .userId(user.getId())
      .email(user.getEmail())
      .role(user.getRole().name())
      .name(name)
      .identificationNumber(idNumber)
      .createdAt(user.getCreatedAt())
      .documentUrl(docUrl)
      .build();
  }
}
