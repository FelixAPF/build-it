package com.build_it.buildit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "business_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
  private User user;

  @Column(name = "company_name", nullable = false, length = 150)
  private String companyName;

  @Column(name = "phone_number", nullable = false, length = 20)
  private String phoneNumber;

  @Column(name = "rbq_number", nullable = false, unique = true, length = 50)
  private String rbqNumber;

  @Column(name = "billing_address")
  private String billingAddress;

  @Column(name = "average_rating")
  private Double averageRating;
}
