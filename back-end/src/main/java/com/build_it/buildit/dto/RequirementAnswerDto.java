package com.build_it.buildit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class RequirementAnswerDto {
  private String questionFr;
  private String questionEn;
  private Long question;
  private String answer;
}
