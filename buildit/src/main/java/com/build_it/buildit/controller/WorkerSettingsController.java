package com.build_it.buildit.controller;

import com.build_it.buildit.entity.JobType;
import com.build_it.buildit.entity.User;
import com.build_it.buildit.entity.WorkerProfile;
import com.build_it.buildit.repository.UserRepository;
import com.build_it.buildit.repository.WorkerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/worker/settings")
@RequiredArgsConstructor
public class WorkerSettingsController {

  private final UserRepository userRepository;
  private final WorkerProfileRepository workerProfileRepository;

  @GetMapping("/profile")
  public ResponseEntity<?> getProfile(Principal principal) {
    User user = userRepository.findByEmail(principal.getName()).orElseThrow();
    WorkerProfile profile = workerProfileRepository.findByUser(user).orElseThrow();

    return ResponseEntity.ok(Map.of(
      "firstName", profile.getFullName().split(" ")[0],
      "lastName", profile.getFullName().substring(profile.getFullName().indexOf(" ") + 1),
      "email", user.getEmail(),
      "specialties", profile.getSpecialties()
    ));
  }

  @PutMapping("/specialties")
  public ResponseEntity<?> updateSpecialties(Principal principal, @RequestBody Map<String, List<JobType>> request) {
    User user = userRepository.findByEmail(principal.getName()).orElseThrow();
    WorkerProfile profile = workerProfileRepository.findByUser(user).orElseThrow();

    profile.setSpecialties(new java.util.HashSet<>(request.get("specialties")));
    workerProfileRepository.save(profile);

    return ResponseEntity.ok(Map.of("message", "Updated successfully"));
  }

  @Transactional
  @DeleteMapping("/account")
  public ResponseEntity<?> deleteAccount(Principal principal) {
    User user = userRepository.findByEmail(principal.getName()).orElseThrow();

    // COMPLIANT SOFT-DELETE:
    // The worker has historical job applications. We must retain their Name, CCQ, and Email
    // for the contractor's legal/tax audit trail.

    // We lock the account and destroy the password so nobody can ever log into it again.
    user.setStatus(com.build_it.buildit.entity.AccountStatus.SUSPENDED);
    user.setPasswordHash("");
    userRepository.save(user);

    return ResponseEntity.ok(Map.of("message", "Account successfully deactivated."));
  }
}
