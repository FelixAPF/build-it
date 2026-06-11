package com.build_it.buildit.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ApplicationResponse {
  private Long applicationId;  // The ID the business needs to click "Approve"
  private Long workerId;
  private String workerName;
  private Integer yearsExperience;
  private Double averageRating;
  private LocalDateTime appliedAt;
  private String status;
}
