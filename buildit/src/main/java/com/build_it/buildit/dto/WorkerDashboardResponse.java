package com.build_it.buildit.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class WorkerDashboardResponse {
  private Long applicationId;
  private Long jobId;
  private String companyName;
  private String companyPhone;
  private String companyEmail;
  private String address;
  private LocalDateTime startDatetime;
  private LocalDateTime endDatetime;
  private Boolean isTimeFlexible;
  private Boolean providesSupplyChain;
  private List<String> specificTools;
  private List<String> supplyChainItems;
  private String jobType;
  private String paymentType;
  private Double payRate;
  private String applicationStatus;
  private String jobStatus;
  private boolean reviewedBusiness;

  // FIX: Added the answers list so the builder works
  private List<RequirementAnswerDto> answers;
}
