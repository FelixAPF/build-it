package com.build_it.buildit.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class RequirementDetailDto {
  private Long requirementId;
  private String jobType;
  private Double hourlyRate;
  private Integer qtyRequested;
  private Integer qtyFilled;
  private List<AssignedWorkerDto> assignedWorkers;
  private long pendingApplicantsCount;
}
