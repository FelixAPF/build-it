package com.build_it.buildit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_requirements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRequirement {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "job_posting_id", nullable = false)
  private JobPosting jobPosting;

  @Enumerated(EnumType.STRING)
  @Column(name = "job_type", nullable = false, length = 50)
  private JobType jobType;

  @Column(name = "hourly_rate", nullable = false)
  private Double hourlyRate;

  @Column(name = "qty_requested", nullable = false)
  private Integer qtyRequested;

  @Builder.Default
  @Column(name = "qty_filled", nullable = false)
  private Integer qtyFilled = 0;

  // Tracks all workers who applied specifically to this requirement
  @OneToMany(mappedBy = "jobRequirement", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<JobApplication> applications = new ArrayList<>();
}
