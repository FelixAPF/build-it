package com.build_it.buildit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BusinessRegisterRequest {
  @NotBlank(message = "Account intention is required")
  private String businessType;

  @NotBlank(message = "Company or Name is required")
  private String companyName;

  @NotBlank(message = "Contact name is required")
  private String contactName;

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  private String email;

  @NotBlank(message = "Password is required")
  private String password;

  @NotBlank(message = "Phone number is required")
  private String phoneNumber;

  // --- NEW ADDRESS SUITE ---
  @NotBlank(message = "Street address is required")
  private String address;

  @NotBlank(message = "City is required")
  private String city;

  @NotBlank(message = "Province is required")
  private String province;

  @NotBlank(message = "Postal Code is required")
  private String postalCode;

  private String rbqNumber;
  private String neqNumber;
}
