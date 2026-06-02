package com.build_it.buildit.service;

import com.build_it.buildit.dto.UserAdminResponse;
import com.build_it.buildit.entity.AccountStatus;
import com.build_it.buildit.entity.Role;
import com.build_it.buildit.entity.User;
import com.build_it.buildit.repository.BusinessProfileRepository;
import com.build_it.buildit.repository.UserRepository;
import com.build_it.buildit.repository.WorkerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

  private final UserRepository userRepository;
  private final WorkerProfileRepository workerProfileRepository;
  private final BusinessProfileRepository businessProfileRepository;

  @Transactional(readOnly = true)
  public List<UserAdminResponse> getPendingUsers() {
    return userRepository.findByStatus(AccountStatus.PENDING_VERIFICATION).stream().map(user -> {
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
          idNumber = business.getRbqNumber();
        }
      }

      return UserAdminResponse.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .role(user.getRole().name())
        .name(name)
        .identificationNumber(idNumber)
        .createdAt(user.getCreatedAt())
        .build();
    }).collect(Collectors.toList());
  }

  @Transactional
  public String verifyUserAccount(Long userId) {
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

    if (user.getStatus() == AccountStatus.ACTIVE) return "User is already ACTIVE.";

    user.setStatus(AccountStatus.ACTIVE);
    userRepository.save(user);
    return "User account for " + user.getEmail() + " has been successfully verified.";
  }
}
