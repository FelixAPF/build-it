package com.build_it.buildit.dto;

import com.build_it.buildit.entity.JobType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AvailableJobResponse {
  private Long requirementId; // The ID the worker will use to apply
  private Long jobPostingId;
  private String companyName;
  private String address;
  private LocalDateTime startDatetime;
  private LocalDateTime endDatetime;
  private JobType jobType;
  private Double hourlyRate;
  private Integer remainingSpots;
  private Boolean isNewShift;
}
