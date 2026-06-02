package com.build_it.buildit.dto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssignedWorkerDto {
  private Long applicationId;
  private Long workerId;
  private String fullName;
  private String phoneNumber;
  private Double averageRating;
}
