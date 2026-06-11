package com.build_it.buildit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequirementAnswerDto {
  private String question;
  private String answer;
}
