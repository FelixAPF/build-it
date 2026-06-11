package com.build_it.buildit.service;

import com.build_it.buildit.entity.AuditLog;
import com.build_it.buildit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation; // <-- IMPORT THIS
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

  private final AuditLogRepository auditLogRepository;

  // ADD PROPAGATION TO FORCE A NEW WRITE TRANSACTION
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void log(String actor, String action, String details) {
    AuditLog log = AuditLog.builder()
      .timestamp(LocalDateTime.now())
      .actor(actor)
      .action(action)
      .details(details)
      .build();
    auditLogRepository.save(log);
  }

  @Transactional(readOnly = true)
  public List<AuditLog> getAllLogs() {
    return auditLogRepository.findAllByOrderByTimestampDesc();
  }

  @Transactional(readOnly = true)
  public List<AuditLog> getLogsByActor(String actor) {
    return auditLogRepository.findByActorOrderByTimestampDesc(actor);
  }
}
