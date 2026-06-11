package com.build_it.buildit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "email_verification_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String token;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(nullable = false, name = "user_id")
  private User user;
}
