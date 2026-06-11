package com.build_it.buildit.repository;

import com.build_it.buildit.entity.JobPosting;
import com.build_it.buildit.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
  // For the Business Dashboard to see all their postings
  List<JobPosting> findByBusinessIdOrderByStartDatetimeDesc(Long businessId);

  // For the general job board (only showing open or partially filled jobs)
  List<JobPosting> findByStatusIn(List<JobStatus> statuses);
  long countByStatus(JobStatus status);
}
