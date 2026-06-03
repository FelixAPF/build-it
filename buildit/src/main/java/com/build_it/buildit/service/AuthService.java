package com.build_it.buildit.service;

import com.build_it.buildit.dto.*;
import com.build_it.buildit.entity.*;
import com.build_it.buildit.repository.*;
import com.build_it.buildit.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final WorkerProfileRepository workerProfileRepository;
  private final BusinessProfileRepository businessProfileRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuditLogService auditLogService;
  private final JwtUtils jwtUtils;

  @Transactional
  public AuthResponse registerWorker(WorkerRegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new RuntimeException("Email already in use");
    }
    if (workerProfileRepository.existsByCcqNumber(request.getCcqNumber())) {
      throw new RuntimeException("CCQ Number already registered");
    }

    User user = User.builder()
      .email(request.getEmail())
      .passwordHash(passwordEncoder.encode(request.getPassword()))
      .role(Role.WORKER)
      .status(AccountStatus.PENDING_VERIFICATION) // Manual validation phase required
      .build();

    userRepository.save(user);

    WorkerProfile profile = WorkerProfile.builder()
      .user(user)
      .fullName(request.getFullName())
      .phoneNumber(request.getPhoneNumber())
      .ccqNumber(request.getCcqNumber())
      .yearsExperience(request.getYearsExperience())
      .specialties(request.getSpecialties())
      .build();

    workerProfileRepository.save(profile);

    String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
    auditLogService.log(request.getEmail(), "ACCOUNT_REGISTRATION", "Registered as worker profile matching specialization requirements.");
    return new AuthResponse(token, user.getEmail(), user.getRole().name(), user.getStatus().name());
  }

  @Transactional
  public AuthResponse registerBusiness(BusinessRegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new RuntimeException("Email already in use");
    }

    if ("COMPANY".equals(request.getBusinessType())) {
      if (request.getRbqNumber() == null || request.getRbqNumber().trim().isEmpty()) {
        throw new RuntimeException("RBQ Number is strictly required for Construction Companies.");
      }
      if (request.getCcqNumber() == null || request.getCcqNumber().trim().isEmpty()) {
        throw new RuntimeException("CCQ Employer ID is strictly required for Construction Companies.");
      }
      if (businessProfileRepository.existsByRbqNumber(request.getRbqNumber())) {
        throw new RuntimeException("RBQ Number already registered in our system.");
      }
    }

    // FIX: Auto-activate private individuals, but hold companies for review!
    AccountStatus initialStatus = "PRIVATE".equals(request.getBusinessType())
      ? AccountStatus.ACTIVE
      : AccountStatus.PENDING_VERIFICATION;

    User user = User.builder()
      .email(request.getEmail())
      .passwordHash(passwordEncoder.encode(request.getPassword()))
      .role(Role.BUSINESS)
      .status(initialStatus) // <-- Applied here
      .build();

    userRepository.save(user);

    BusinessProfile profile = BusinessProfile.builder()
      .user(user)
      .businessType(request.getBusinessType())
      .companyName(request.getCompanyName())
      .contactName(request.getContactName())
      .phoneNumber(request.getPhoneNumber())
      .rbqNumber(request.getRbqNumber())
      .ccqNumber(request.getCcqNumber())
      .billingAddress(request.getBillingAddress())
      .build();

    businessProfileRepository.save(profile);

    String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
    auditLogService.log(request.getEmail(), "ACCOUNT_REGISTRATION", "Registered as type context: " + request.getBusinessType());
    return new AuthResponse(token, user.getEmail(), user.getRole().name(), user.getStatus().name());
  }

  public AuthResponse login(LoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
      .orElseThrow(() -> new RuntimeException("Invalid credentials"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new RuntimeException("Invalid credentials");
    }

    if (user.getStatus() == AccountStatus.SUSPENDED) {
      throw new RuntimeException("Account has been suspended");
    }

    // Allow PENDING and ACTIVE users to get a token!
    String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
    auditLogService.log(user.getEmail(), "USER_LOGIN", "Authenticated successfully using clean secure route parameters.");
    return new AuthResponse(token, user.getEmail(), user.getRole().name(), user.getStatus().name());
  }
}
