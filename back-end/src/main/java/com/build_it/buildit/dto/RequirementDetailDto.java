package com.build_it.buildit.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class RequirementDetailDto {
  private Long requirementId;
  private String jobType;
  private String paymentType;
  private Double payRate;
  private Integer qtyRequested;
  private Integer qtyFilled;
  private Long pendingApplicantsCount;
  private List<AssignedWorkerDto> assignedWorkers;

  // FIX: Added the answers list so the builder works
  private List<RequirementAnswerDto> answers;
}
