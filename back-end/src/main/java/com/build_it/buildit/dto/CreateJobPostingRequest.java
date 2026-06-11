package com.build_it.buildit.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateJobPostingRequest {

  @NotBlank(message = "Street address is required")
  private String address;

  @NotBlank(message = "City is required")
  private String city;

  @NotBlank(message = "Province is required")
  private String province;

  @NotBlank(message = "Postal Code is required")
  private String postalCode;

  @NotNull(message = "Start date and time is required")
  private LocalDateTime startDatetime;

  @NotNull(message = "End date and time is required")
  private LocalDateTime endDatetime;

  private Boolean isTimeFlexible;
  private Boolean providesSupplyChain;
  private List<String> specificTools;
  private List<String> supplyChainItems; // <-- NEW

  @NotEmpty(message = "At least one job requirement is needed")
  @Valid
  private List<JobRequirementRequest> requirements;
}
