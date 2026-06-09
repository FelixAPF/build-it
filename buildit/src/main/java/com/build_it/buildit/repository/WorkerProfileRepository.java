package com.build_it.buildit.repository;

import com.build_it.buildit.entity.User;
import com.build_it.buildit.entity.WorkerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkerProfileRepository extends JpaRepository<WorkerProfile, Long> {
  Optional<WorkerProfile> findByUserId(Long userId);
  boolean existsByCcqNumber(String ccqNumber);
  Optional<WorkerProfile> findByUser(User user);
}
