package com.build_it.buildit.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BusinessDashboardResponse {
  private Long jobPostingId;
  private String address;
  private LocalDateTime startDatetime;
  private LocalDateTime endDatetime;
  private String status;
  private List<RequirementDetailDto> requirements;
}
