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

  @Column(name = "business_type", nullable = false, length = 50)
  private String businessType;

  @Column(name = "company_name", nullable = false, length = 150)
  private String companyName;

  @Column(name = "contact_name", nullable = false, length = 150)
  private String contactName;

  @Column(name = "phone_number", nullable = false, length = 20)
  private String phoneNumber;

  @Column(name = "rbq_number", unique = true, length = 50)
  private String rbqNumber;

  @Column(name = "neq_number", length = 50)
  private String neqNumber;

  // --- NEW ADDRESS SUITE ---
  @Column(nullable = false, length = 255)
  private String address;

  @Column(nullable = false, length = 100)
  private String city;

  @Column(nullable = false, length = 50)
  private String province;

  @Column(name = "postal_code", nullable = false, length = 20)
  private String postalCode;

  @Column(name = "average_rating")
  private Double averageRating;
}
