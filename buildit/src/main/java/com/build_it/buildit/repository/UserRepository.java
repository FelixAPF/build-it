package com.build_it.buildit.repository;

import com.build_it.buildit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  // We will need this exact method for Spring Security Login
  Optional<User> findByEmail(String email);

  // Quick check to prevent duplicate registrations
  boolean existsByEmail(String email);
}
