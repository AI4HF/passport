package io.passport.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);;

    @Autowired
    public AuditLogBookService(AuditLogBookRepository auditLogBookRepository, AuditLogRepository auditLogRepository) {
        this.auditLogBookRepository = auditLogBookRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public List<AuditLogBook> getAuditLogBooksByPassportId(Long passportId) {
        return auditLogBookRepository.findByIdPassportId(passportId);
    }

    public void createAuditLogBookEntries(Long passportId, Long studyId, Long deploymentId) {
        List<AuditLog> relatedAuditLogs = auditLogRepository.findAll()
                .stream()
                .filter(log -> isRelatedToPassport(log, studyId, deploymentId))
                .toList();

        for (AuditLog auditLog : relatedAuditLogs) {
            AuditLogBook auditLogBook = new AuditLogBook(new AuditLogBookId(passportId, auditLog.getAuditLogId()));
            auditLogBookRepository.save(auditLogBook);
        }
    }

    private boolean isRelatedToPassport(AuditLog auditLog, Long studyId, Long deploymentId) {
        return auditLog.getStudyId().equals(studyId) ||
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
            String username,
            Long studyId,
            String actionType,
            String affectedRelation,
            String recordId,
            Object entity,
            String description
    ) {

        String recordData = (entity != null)
                ? objectToJsonSafely(entity)
                : "None";

        AuditLog auditLog = new AuditLog();
        auditLog.setPersonId(userId);
        auditLog.setPersonName(username);
        auditLog.setStudyId(studyId);
        auditLog.setActionType(actionType);
        auditLog.setAffectedRelation(affectedRelation);
        auditLog.setAffectedRecordId(recordId);
        auditLog.setAffectedRecord(recordData);
        auditLog.setDescription(description);
        auditLog.setOccurredAt(Instant.now());

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

