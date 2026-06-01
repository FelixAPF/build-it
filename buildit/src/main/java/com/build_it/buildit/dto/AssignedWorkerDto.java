package com.build_it.buildit.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssignedWorkerDto {
  private Long workerId;
  private String fullName;
  private String phoneNumber; // So the business can call the worker
  private Double averageRating;
}
