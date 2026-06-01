package com.build_it.buildit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BusinessRegisterRequest {
  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  private String email;

  @NotBlank(message = "Password is required")
  private String password;

  @NotBlank(message = "Company name is required")
  private String companyName;

  @NotBlank(message = "Phone number is required")
  private String phoneNumber;

  @NotBlank(message = "RBQ number is required")
  private String rbqNumber;

  @NotBlank(message = "Billing address is required")
  private String billingAddress;
}
