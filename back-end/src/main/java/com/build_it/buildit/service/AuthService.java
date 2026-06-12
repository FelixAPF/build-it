package com.build_it.buildit.service;

import com.build_it.buildit.dto.*;
import com.build_it.buildit.entity.*;
import com.build_it.buildit.repository.*;
import com.build_it.buildit.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final WorkerProfileRepository workerProfileRepository;
  private final BusinessProfileRepository businessProfileRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailVerificationTokenRepository emailVerificationTokenRepository;
  private final AuditLogService auditLogService;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final EmailService emailService;
  private final JwtUtils jwtUtils;

  @Transactional
  public String registerWorker(WorkerRegisterRequest request) {
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
      .status(AccountStatus.UNVERIFIED) // Manual validation phase required
      .build();

    userRepository.save(user);

    WorkerProfile profile = WorkerProfile.builder()
      .user(user)
      // FIX: Map the specific first and last name fields
      .firstName(request.getFirstName())
      .lastName(request.getLastName())
      .phoneNumber(request.getPhoneNumber())
      .ccqNumber(request.getCcqNumber())
      .yearsExperience(request.getYearsExperience())
      .specialties(request.getSpecialties())
      .build();

    workerProfileRepository.save(profile);

    String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
    auditLogService.log(request.getEmail(), "ACCOUNT_REGISTRATION", "Registered as worker profile matching specialization requirements.");
    emailVerificationTokenRepository.save(EmailVerificationToken.builder().user(user).token(token).build());
    emailService.sendVerificationEmail(user.getEmail(), token);
    return "Registration successful! Please check your email to verify your account.";
  }

  @Transactional
  public String registerBusiness(BusinessRegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new RuntimeException("Email already in use");
    }

    if ("COMPANY".equals(request.getBusinessType())) {
      if (request.getRbqNumber() == null || request.getRbqNumber().trim().isEmpty()) {
        throw new RuntimeException("RBQ Number is strictly required for Construction Companies.");
      }
      if (request.getNeqNumber() == null || request.getNeqNumber().trim().isEmpty()) {
        throw new RuntimeException("NEQ Number is strictly required for Construction Companies.");
      }
      if (businessProfileRepository.existsByRbqNumber(request.getRbqNumber())) {
        throw new RuntimeException("RBQ Number already registered in our system.");
      }
      if (businessProfileRepository.existsByNeqNumber(request.getNeqNumber())) {
        throw new RuntimeException("NEQ Number already registered in our system.");
      }
    }

    // Auto-activate private individuals, but hold companies for review!
    AccountStatus initialStatus = AccountStatus.UNVERIFIED;

    User user = User.builder()
      .email(request.getEmail())
      .passwordHash(passwordEncoder.encode(request.getPassword()))
      .role(Role.BUSINESS)
      .status(initialStatus)
      .build();

    userRepository.save(user);

    BusinessProfile profile = BusinessProfile.builder()
      .user(user)
      .businessType(request.getBusinessType())
      .companyName(request.getCompanyName())
      .contactName(request.getContactName())
      .phoneNumber(request.getPhoneNumber())
      .rbqNumber(request.getRbqNumber())
      .neqNumber(request.getNeqNumber())
      .address(request.getAddress())
      .city(request.getCity())
      .province(request.getProvince())
      .postalCode(request.getPostalCode())
      .build();

    businessProfileRepository.save(profile);

    String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
    auditLogService.log(request.getEmail(), "ACCOUNT_REGISTRATION", "Registered as type context: " + request.getBusinessType());
    emailVerificationTokenRepository.save(EmailVerificationToken.builder().user(user).token(token).build());
    emailService.sendVerificationEmail(user.getEmail(), token);
    return "Registration successful! Please check your email to verify your account.";
  }

  public AuthResponse login(LoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
      .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
        org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid email or password."));

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new org.springframework.web.server.ResponseStatusException(
        org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid email or password.");
    }

    if (user.getStatus() == AccountStatus.SUSPENDED) {
      throw new RuntimeException("Account has been suspended");
    }

    if (user.getStatus() == AccountStatus.UNVERIFIED) {
      throw new RuntimeException("Please verify your email address before logging in.");
    }

    String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
    auditLogService.log(user.getEmail(), "USER_LOGIN", "Authenticated successfully using clean secure route parameters.");
    return new AuthResponse(token, user.getEmail(), user.getRole().name(), user.getStatus().name());
  }

  @Transactional(readOnly = true)
  public java.util.Map<String, String> getBusinessProfileContext(String email) {
    User user = userRepository.findByEmail(email)
      .orElseThrow(() -> new RuntimeException("User not found"));

    BusinessProfile profile = businessProfileRepository.findByUserId(user.getId())
      .orElseThrow(() -> new RuntimeException("Business profile not found"));

    java.util.Map<String, String> context = new java.util.HashMap<>();
    context.put("businessType", profile.getBusinessType());
    context.put("companyName", profile.getCompanyName());
    context.put("id", String.valueOf(profile.getId()));
    return context;
  }

  @Transactional
  public String verifyEmail(String token) {
    EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
      .orElseThrow(() -> new RuntimeException("Invalid or expired verification link."));

    User user = verificationToken.getUser();

    if (user.getRole() == Role.WORKER) {
      user.setStatus(AccountStatus.PENDING_UPLOAD);
    } else if (user.getRole() == Role.BUSINESS) {
      BusinessProfile profile = businessProfileRepository.findByUserId(user.getId())
        .orElseThrow(() -> new RuntimeException("Profile not found"));

      if ("PRIVATE".equals(profile.getBusinessType())) {
        user.setStatus(AccountStatus.ACTIVE);
      } else {
        user.setStatus(AccountStatus.PENDING_UPLOAD);
      }
    }

    userRepository.save(user);
    emailVerificationTokenRepository.delete(verificationToken);
    auditLogService.log(user.getEmail(), "EMAIL_VERIFIED", "User successfully verified their email address.");

    return "Email successfully verified! You can now log in.";
  }

  @Transactional
  public String completeDocumentUpload(String email, String fileName) {
    User user = userRepository.findByEmail(email)
      .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getStatus() != AccountStatus.PENDING_UPLOAD) {
      throw new RuntimeException("Account is not currently pending document uploads.");
    }

    user.setDocumentPath(fileName);
    user.setStatus(AccountStatus.PENDING_VERIFICATION);
    userRepository.save(user);
    auditLogService.log(email, "DOCUMENT_UPLOAD", "User uploaded compliance documents. Account is now queued for administrative review.");

    return "Documents successfully uploaded. Your account is now under review.";
  }

  @Transactional
  public String processForgotPassword(String email) {
    Optional<User> userOpt = userRepository.findByEmail(email);

    if (userOpt.isEmpty()) {
      return "If this email is registered, a password reset link has been sent.";
    }

    User user = userOpt.get();

    passwordResetTokenRepository.deleteByUserId(user.getId());

    String token = java.util.UUID.randomUUID().toString();
    PasswordResetToken resetToken = PasswordResetToken.builder()
      .user(user)
      .token(token)
      .expiryDate(LocalDateTime.now().plusHours(1))
      .build();

    passwordResetTokenRepository.save(resetToken);
    emailService.sendPasswordResetEmail(user.getEmail(), token);

    return "If this email is registered, a password reset link has been sent.";
  }

  @Transactional
  public String resetPassword(String token, String newPassword) {
    PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
      .orElseThrow(() -> new RuntimeException("Invalid or expired password reset token."));

    if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
      passwordResetTokenRepository.delete(resetToken);
      throw new RuntimeException("This password reset link has expired. Please request a new one.");
    }

    User user = resetToken.getUser();
    user.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    passwordResetTokenRepository.delete(resetToken);

    return "Your password has been successfully reset. You can now log in.";
  }
}
