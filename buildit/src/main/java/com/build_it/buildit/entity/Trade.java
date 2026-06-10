package com.build_it.buildit.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "trades")
@Data
public class Trade {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String value;

  @Column(name = "label_en", nullable = false)
  private String labelEn;

  @Column(name = "label_fr", nullable = false)
  private String labelFr;
}
