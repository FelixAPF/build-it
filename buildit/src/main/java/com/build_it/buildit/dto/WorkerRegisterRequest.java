package com.build_it.buildit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.Set;

@Data
public class WorkerRegisterRequest {
  @NotBlank(message = "First name is required")
  private String firstName;

  @NotBlank(message = "Last name is required")
  private String lastName;

  @Email(message = "Invalid email format")
  @NotBlank(message = "Email is required")
  private String email;

  @NotBlank(message = "Password is required")
  private String password;

  @NotBlank(message = "Phone number is required")
  private String phoneNumber;

  @Min(value = 0, message = "Years of experience cannot be negative")
  private Integer yearsExperience;

  private String ccqNumber;

  @NotEmpty(message = "At least one specialty is required")
  private Set<String> specialties;

  // RESTORED: Helper method for AuthService
  public String getFullName() {
    return this.firstName + " " + this.lastName;
  }
}
