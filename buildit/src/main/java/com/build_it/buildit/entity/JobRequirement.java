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
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private JobPosting jobPosting;

  // CHANGED FROM ENUM TO STRING
  @Column(name = "job_type", nullable = false, length = 50)
  private String jobType;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_type", length = 30)
  private PaymentType paymentType;

  @Column(name = "pay_rate")
  private Double payRate;

  @Column(name = "qty_requested", nullable = false)
  private Integer qtyRequested;

  @Column(name = "qty_filled", nullable = false)
  private Integer qtyFilled;

  @ElementCollection
  @CollectionTable(name = "job_requirement_answers", joinColumns = @JoinColumn(name = "job_requirement_id"))
  @Builder.Default
  private List<RequirementAnswer> answers = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "jobRequirement", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<JobApplication> assignedWorkers = new ArrayList<>();
}
