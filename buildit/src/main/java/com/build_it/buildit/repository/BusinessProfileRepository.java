package com.build_it.buildit.repository;

import com.build_it.buildit.entity.BusinessProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {
  Optional<BusinessProfile> findByUserId(Long userId);
  boolean existsByRbqNumber(String rbqNumber);
}
