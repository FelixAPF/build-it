package com.build_it.buildit.repository;

import com.build_it.buildit.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
  Optional<PasswordResetToken> findByToken(String token);
  void deleteByUserId(Long userId); // Used to wipe out old tokens if they request a new one
}
