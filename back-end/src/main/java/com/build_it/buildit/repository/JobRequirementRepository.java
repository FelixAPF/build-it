package com.build_it.buildit.repository;

import com.build_it.buildit.entity.JobRequirement;
import com.build_it.buildit.entity.JobStatus;
import com.build_it.buildit.entity.JobType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface JobRequirementRepository extends JpaRepository<JobRequirement, Long> {
  List<JobRequirement> findByJobPostingId(Long jobPostingId);

  // NEW: Fetches requirements that match the worker's trades, still have room, and the parent job is still open
  @Query("SELECT jr FROM JobRequirement jr JOIN FETCH jr.jobPosting jp " +
    "WHERE jr.jobType IN :specialties " +
    "AND jr.qtyFilled < jr.qtyRequested " +
    "AND jp.status IN :statuses")
  List<JobRequirement> findAvailableRequirements(
    @Param("specialties") Set<JobType> specialties,
    @Param("statuses") List<JobStatus> statuses);
}
