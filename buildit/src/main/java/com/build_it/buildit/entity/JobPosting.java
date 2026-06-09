package com.build_it.buildit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_postings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPosting {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "business_id", nullable = false)
  private BusinessProfile business;

  @Column(nullable = false, length = 255)
  private String address;

  @Column(length = 100)
  private String city;

  @Column(length = 50)
  private String province;

  @Column(name = "postal_code", length = 20)
  private String postalCode;

  @Column(name = "start_datetime", nullable = false)
  private LocalDateTime startDatetime;

  @Column(name = "end_datetime", nullable = false)
  private LocalDateTime endDatetime;

  @Column(name = "is_time_flexible")
  private Boolean isTimeFlexible;

  @Column(name = "provides_supply_chain")
  private Boolean providesSupplyChain;

  @ElementCollection
  @CollectionTable(name = "job_specific_tools", joinColumns = @JoinColumn(name = "job_posting_id"))
  @Column(name = "tool_name")
  @Builder.Default
  private List<String> specificTools = new ArrayList<>();

  // NEW: Dynamic List of Supply Chain Items
  @ElementCollection
  @CollectionTable(name = "job_supply_chain_items", joinColumns = @JoinColumn(name = "job_posting_id"))
  @Column(name = "item_name")
  @Builder.Default
  private List<String> supplyChainItems = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private JobStatus status;

  @Column(name = "total_app_fee_charged")
  private Double totalAppFeeCharged;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @Builder.Default
  @OneToMany(mappedBy = "jobPosting", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<JobRequirement> requirements = new ArrayList<>();
}
