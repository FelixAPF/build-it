package com.build_it.buildit.repository;

import com.build_it.buildit.entity.JobRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRequirementRepository extends JpaRepository<JobRequirement, Long> {
  List<JobRequirement> findByJobPostingId(Long jobPostingId);
}
