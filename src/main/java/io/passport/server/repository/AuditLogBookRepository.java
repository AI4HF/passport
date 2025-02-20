package io.passport.server.repository;

import io.passport.server.model.AuditLogBook;
import io.passport.server.model.AuditLogBookId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AuditLogBook repository for database management.
 */
@Repository
public interface AuditLogBookRepository extends JpaRepository<AuditLogBook, AuditLogBookId> {
    /**
     * Find all AuditLogBook entries by Passport ID.
     *
     * @param passportId The ID of the passport.
     * @return List of AuditLogBook entries.
     */
    List<AuditLogBook> findByIdPassportId(Long passportId);

    /**
     * Find all AuditLogBook entries by Audit Log ID.
     *
     * @param auditLogId The ID of the audit log.
     * @return List of AuditLogBook entries.
     */
    List<AuditLogBook> findByIdAuditLogId(String auditLogId);
}
