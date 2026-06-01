package com.build_it.buildit.entity;

import jakarta.persistence.*;
import lombok.*;
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

  // This creates a 1-to-1 relationship with the User table
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
  private User user;

  @Column(name = "full_name", nullable = false, length = 100)
  private String fullName;

  @Column(name = "phone_number", nullable = false, length = 20)
  private String phoneNumber;

  @Column(name = "ccq_number", nullable = false, unique = true, length = 50)
  private String ccqNumber;

  @Column(name = "years_experience")
  private Integer yearsExperience;

  @Column(name = "average_rating")
  private Double averageRating;

  // This automatically creates a secondary table `worker_specialties` to hold multiple job types per worker
  @ElementCollection(targetClass = JobType.class, fetch = FetchType.EAGER)
  @CollectionTable(name = "worker_specialties", joinColumns = @JoinColumn(name = "worker_profile_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "job_type")
  private Set<JobType> specialties;
}
