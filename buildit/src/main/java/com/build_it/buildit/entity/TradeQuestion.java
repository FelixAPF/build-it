package com.build_it.buildit.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "trade_questions")
@Data
public class TradeQuestion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // CHANGED FROM ENUM TO STRING
  @Column(name = "job_type", nullable = false, length = 50)
  private String jobType;

  @Column(nullable = false)
  private String questionText;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "trade_question_options", joinColumns = @JoinColumn(name = "trade_question_id"))
  @Column(name = "option_text")
  private List<String> options;
}
