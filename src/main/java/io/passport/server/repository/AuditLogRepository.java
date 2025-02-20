package io.passport.server.repository;

import io.passport.server.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AuditLog repository for database management.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
    /**
     * Find all AuditLogs by a list of AuditLog IDs.
     *
     * @param auditLogIds List of AuditLog IDs.
     * @return List of AuditLog entries.
     */
    List<AuditLog> findByAuditLogIdIn(List<String> auditLogIds);
}
