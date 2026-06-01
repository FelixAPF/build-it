package com.build_it.buildit.dto;

import com.build_it.buildit.entity.JobType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobRequirementRequest {

  @NotNull(message = "Job type is required")
  private JobType jobType;

  @NotNull(message = "Hourly rate is required")
  @Min(value = 15, message = "Minimum wage compliance required") // Example minimum
  private Double hourlyRate;

  @NotNull(message = "Quantity is required")
  @Min(value = 1, message = "Must request at least 1 worker")
  private Integer qtyRequested;
}
