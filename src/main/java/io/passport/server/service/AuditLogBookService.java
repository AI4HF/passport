package io.passport.server.service;

import io.passport.server.model.AuditLog;
import io.passport.server.model.AuditLogBook;
import io.passport.server.model.AuditLogBookId;
import io.passport.server.model.Operation;
import io.passport.server.repository.AuditLogBookRepository;
import io.passport.server.repository.AuditLogRepository;
import io.passport.server.util.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Service class for Audit Log Book management.
 */
@Service
public class AuditLogBookService {

    /**
     * AuditLogBook repo access for database management.
     */
    private final AuditLogBookRepository auditLogBookRepository;
    /**
     * AuditLog repo access for database management.
     */
    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditLogBookService(AuditLogBookRepository auditLogBookRepository, AuditLogRepository auditLogRepository) {
        this.auditLogBookRepository = auditLogBookRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Return all AuditLogBooks based on passport id.
     * @param passportId Connected passport id.
     * @return
     */
    public List<AuditLogBook> getAuditLogBooksByPassportId(Long passportId) {
        return auditLogBookRepository.findByIdPassportId(passportId);
    }

    /**
     * Create AuditLogBook entries.
     * @param passportId Connected passport id.
     * @param studyId Connected study id.
     */
    public void createAuditLogBookEntries(Long passportId, Long studyId) {
        List<AuditLog> relatedAuditLogs = auditLogRepository.findAll()
                .stream()
                .filter(log -> isRelatedToPassport(log, studyId))
                .toList();

        for (AuditLog auditLog : relatedAuditLogs) {
            AuditLogBook auditLogBook = new AuditLogBook(new AuditLogBookId(passportId, auditLog.getAuditLogId()));
            auditLogBookRepository.save(auditLogBook);
        }
    }

    /**
     * Check if an Audit Log belongs to a certain study.
     * @param auditLog Subject of comparison.
     * @param studyId Compared study id.
     * @return
     */
    private boolean isRelatedToPassport(AuditLog auditLog, Long studyId) {
        return auditLog.getStudyId().equals(studyId);
    }

    /**
     * Get all Audit Logs with given ids.
     * @param auditLogIds List of ids to be retrieved.
     * @return
     */
    public List<AuditLog> getAuditLogsByIds(List<String> auditLogIds) {
        return auditLogRepository.findByAuditLogIdIn(auditLogIds);
    }

    /**
     * Creates and saves a new AuditLog entry. Optionally, you could also
     * create an AuditLogBook entry in the same method if your domain
     * requires it.
     *
     * @param userId            The ID of the user who performed the action.
     * @param actionType        "CREATE", "UPDATE", or "DELETE", etc.
     * @param affectedRelation  The table/collection name (e.g. "Algorithm").
     * @param recordId          The primary key ID of the affected record.
     * @param entity            The updated/created entity; can be null or a full object.
     * @param description       A short description of the operation.
     * @return                  The saved AuditLog entity.
     */
    public AuditLog createAuditLog(
            String userId,
            String username,
            Long studyId,
            Operation actionType,
            String affectedRelation,
            String recordId,
            Object entity,
            String description
    ) {

        String recordData = (entity != null)
                ? JSONUtil.objectToJsonSafely(entity)
                : "None";

        AuditLog auditLog = new AuditLog();
        auditLog.setPersonId(userId);
        auditLog.setPersonName(username);
        auditLog.setStudyId(studyId);
        auditLog.setActionType(actionType.name());
        auditLog.setAffectedRelation(affectedRelation);
        auditLog.setAffectedRecordId(recordId);
        auditLog.setAffectedRecord(recordData);
        auditLog.setDescription(description);
        auditLog.setOccurredAt(Instant.now());

        return auditLogRepository.save(auditLog);
    }
}