package com.build_it.buildit.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateJobPostingRequest {

  @NotBlank(message = "Address is required")
  private String address;

  @NotNull(message = "Start date and time is required")
  @Future(message = "Start time must be in the future")
  private LocalDateTime startDatetime;

  @NotNull(message = "End date and time is required")
  @Future(message = "End time must be in the future")
  private LocalDateTime endDatetime;

  @NotEmpty(message = "At least one job requirement is needed")
  @Valid
  private List<JobRequirementRequest> requirements;
}
