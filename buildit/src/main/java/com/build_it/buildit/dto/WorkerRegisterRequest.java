package com.build_it.buildit.dto;

import com.build_it.buildit.entity.JobType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class WorkerRegisterRequest {
  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  private String email;

  @NotBlank(message = "Password is required")
  private String password;

  @NotBlank(message = "Full name is required")
  private String fullName;

  @NotBlank(message = "Phone number is required")
  private String phoneNumber;

  @NotBlank(message = "CCQ number is required")
  private String ccqNumber;

  @NotNull(message = "Years of experience is required")
  private Integer yearsExperience;

  @NotEmpty(message = "At least one specialty must be selected")
  private Set<JobType> specialties;
}
