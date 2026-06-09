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
  private String value; // e.g., "ELECTRICIEN"

  @Column(nullable = false)
  private String label; // e.g., "Electrician"
}
