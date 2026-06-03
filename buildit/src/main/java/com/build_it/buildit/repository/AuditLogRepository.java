package com.build_it.buildit.repository;

import com.build_it.buildit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
  List<AuditLog> findAllByOrderByTimestampDesc();

  // NEW: Fetch logs for a specific user
  List<AuditLog> findByActorOrderByTimestampDesc(String actor);
}
