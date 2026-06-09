package com.build_it.buildit.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "trade_questions")
@Data
public class TradeQuestion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private JobType jobType;

  @Column(nullable = false)
  private String questionText;
}
