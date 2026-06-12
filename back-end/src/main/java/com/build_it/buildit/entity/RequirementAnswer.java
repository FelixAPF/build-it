package com.build_it.buildit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequirementAnswer {
  @Column(name = "question_id")
  private Long questionId;

  private String answer;
}
