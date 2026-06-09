package com.build_it.buildit.dto;

import com.build_it.buildit.entity.JobType;
import com.build_it.buildit.entity.PaymentType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AvailableJobResponse {
  private Long requirementId;
  private Long jobPostingId;
  private String companyName;
  private String address;
  private LocalDateTime startDatetime;
  private LocalDateTime endDatetime;

  @JsonProperty("isTimeFlexible")
  private Boolean isTimeFlexible;

  private Boolean providesSupplyChain;
  private List<String> specificTools;
  private List<String> supplyChainItems; // <-- NEW

  private JobType jobType;
  private PaymentType paymentType;
  private Double payRate;
  private Integer remainingSpots;

  @JsonProperty("isNewShift")
  private Boolean isNewShift;
  private List<RequirementAnswerDto> answers;
}
