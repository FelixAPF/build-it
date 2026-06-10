package com.build_it.buildit.repository;

import com.build_it.buildit.entity.ApplicationStatus;
import com.build_it.buildit.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
  // For the Worker Dashboard to see jobs they applied to
  List<JobApplication> findByWorkerId(Long workerId);

  // For the Business to see who applied to a specific slot
  List<JobApplication> findByJobRequirementIdAndStatus(Long jobRequirementId, ApplicationStatus status);

  // We will use this later for the Handshake auto-cancel feature
  List<JobApplication> findByWorkerIdAndStatus(Long workerId, ApplicationStatus status);

  boolean existsByWorkerIdAndJobRequirementId(Long workerId, Long jobRequirementId);
  long countByStatus(ApplicationStatus status);
}
