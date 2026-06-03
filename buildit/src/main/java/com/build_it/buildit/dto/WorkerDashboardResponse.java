package com.build_it.buildit.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class WorkerDashboardResponse {
  private Long applicationId;
  private Long jobId;
  private String companyName;
  private String companyPhone; // Important so the worker can call the boss if they are late
  private String address;
  private LocalDateTime startDatetime;
  private LocalDateTime endDatetime;
  private String jobType;
  private Double hourlyRate;
  private String applicationStatus;
  private String jobStatus;
  private boolean reviewedBusiness;
}
