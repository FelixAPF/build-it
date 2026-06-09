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

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_type",  length = 30)
  private PaymentType paymentType;

  @Column(name = "pay_rate")
  private Double payRate;

  @ElementCollection
  @CollectionTable(name = "job_requirement_answers", joinColumns = @JoinColumn(name = "job_requirement_id"))
  @Builder.Default
  private List<RequirementAnswer> answers = new ArrayList<>();

  @Column(name = "qty_requested", nullable = false)
  private Integer qtyRequested;

  @Builder.Default
  @Column(name = "qty_filled", nullable = false)
  private Integer qtyFilled = 0;

  @Builder.Default
  @OneToMany(mappedBy = "jobRequirement", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<JobApplication> applications = new ArrayList<>();
}
