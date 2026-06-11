package com.build_it.buildit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplication {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "worker_id", nullable = false)
  private WorkerProfile worker;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "job_requirement_id", nullable = false)
  private JobRequirement jobRequirement;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private ApplicationStatus status;

  @CreationTimestamp
  @Column(name = "applied_at", updatable = false)
  private LocalDateTime appliedAt;

  // RESTORED: Review Tracking
  @Column(name = "reviewed_business", nullable = false)
  private boolean reviewedBusiness;

  @Column(name = "reviewed_worker", nullable = false)
  private boolean reviewedWorker;
}
