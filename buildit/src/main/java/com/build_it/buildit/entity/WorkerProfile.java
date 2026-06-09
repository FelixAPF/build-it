package com.build_it.buildit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "worker_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerProfile {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private User user;

  @Column(name = "first_name", nullable = false, length = 50)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 50)
  private String lastName;

  @Column(name = "phone_number", nullable = false, length = 20)
  private String phoneNumber;

  @Column(name = "years_experience", nullable = false)
  private Integer yearsExperience;

  @Column(name = "ccq_number", length = 50)
  private String ccqNumber;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "worker_specialties", joinColumns = @JoinColumn(name = "worker_id"))
  @Column(name = "specialty")
  private Set<String> specialties = new HashSet<>();

  // RESTORED: Rating Tracking
  @Column(name = "average_rating")
  @Builder.Default
  private Double averageRating = 0.0;

  // RESTORED: New Shift Notification Tracking
  @Column(name = "last_feed_check")
  private LocalDateTime lastFeedCheck;

  public String getFullName() {
    return firstName + " " + lastName;
  }
}
