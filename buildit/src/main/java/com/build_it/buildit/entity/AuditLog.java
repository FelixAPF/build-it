package com.build_it.buildit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private LocalDateTime timestamp;

  @Column(nullable = false, length = 150)
  private String actor;

  @Column(nullable = false, length = 100)
  private String action;

  @Column(columnDefinition = "TEXT")
  private String details;
}
