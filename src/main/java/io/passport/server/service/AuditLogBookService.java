package io.passport.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.passport.server.model.AuditLog;
import io.passport.server.model.AuditLogBook;
import io.passport.server.model.AuditLogBookId;
import io.passport.server.repository.AuditLogBookRepository;
import io.passport.server.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AuditLogBookService {

    private final AuditLogBookRepository auditLogBookRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public AuditLogBookService(AuditLogBookRepository auditLogBookRepository, AuditLogRepository auditLogRepository) {
        this.auditLogBookRepository = auditLogBookRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public List<AuditLogBook> getAuditLogBooksByPassportId(String passportId) {
        return auditLogBookRepository.findByIdPassportId(passportId);
    }

    public void createAuditLogBookEntries(String passportId, Long studyId, Long deploymentId) {
        // Query all audit logs related to studyId and deploymentId
        List<AuditLog> relatedAuditLogs = auditLogRepository.findAll()
                .stream()
                .filter(log -> isRelatedToPassport(log, studyId, deploymentId))
                .toList();

        // Create AuditLogBook entries for each related AuditLog
        for (AuditLog auditLog : relatedAuditLogs) {
            AuditLogBook auditLogBook = new AuditLogBook(new AuditLogBookId(passportId, auditLog.getAuditLogId()));
            auditLogBookRepository.save(auditLogBook);
        }
    }

    private boolean isRelatedToPassport(AuditLog auditLog, Long studyId, Long deploymentId) {
        // Logic to determine if the audit log is related to the given passport's study and deployment
        return auditLog.getAffectedRecordId().equals(String.valueOf(studyId)) ||
                auditLog.getAffectedRecordId().equals(String.valueOf(deploymentId));
    }

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
            String actionType,
            String affectedRelation,
            String recordId,
            Object entity,
            String description
    ) {
        String auditLogId = UUID.randomUUID().toString();

        String recordData = (entity != null)
                ? objectToJsonSafely(entity)
                : "None";

        AuditLog auditLog = new AuditLog(
                auditLogId,
                userId,
                Instant.now(),
                actionType,
                affectedRelation,
                recordId,
                recordData,
                description
        );

        // Save the AuditLog to the DB
        return auditLogRepository.save(auditLog);
    }

    /**
     * Converts any object to JSON string safely.
     */
    private String objectToJsonSafely(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "Unable to serialize object: " + e.getMessage();
        }
    }
}

