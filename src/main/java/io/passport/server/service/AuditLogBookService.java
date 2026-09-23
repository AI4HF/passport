package io.passport.server.service;

import io.passport.server.model.*;
import io.passport.server.repository.AuditLogBookRepository;
import io.passport.server.repository.AuditLogRepository;
import io.passport.server.util.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Service class for Audit Log Book management.
 */
@Service
public class AuditLogBookService {

    /**
     * Keycloak names every service-account user after its client.
     */
    private static final String SERVICE_ACCOUNT_USERNAME_PREFIX = "service-account-";

    /**
     * AuditLogBook repo access for database management.
     */
    private final AuditLogBookRepository auditLogBookRepository;
    /**
     * AuditLog repo access for database management.
     */
    private final AuditLogRepository auditLogRepository;
    /**
     * Resolves the service account behind a machine-to-machine token.
     */
    private final SoftwareAgentService softwareAgentService;

    @Autowired
    public AuditLogBookService(AuditLogBookRepository auditLogBookRepository, AuditLogRepository auditLogRepository,
                               SoftwareAgentService softwareAgentService) {
        this.auditLogBookRepository = auditLogBookRepository;
        this.auditLogRepository = auditLogRepository;
        this.softwareAgentService = softwareAgentService;
    }

    /**
     * Return all AuditLogBooks based on passport id.
     * @param passportId Connected passport id.
     * @return
     */
    public List<AuditLogBook> getAuditLogBooksByPassportId(String passportId) {
        return auditLogBookRepository.findByIdPassportId(passportId);
    }

    /**
     * Create AuditLogBook entries.
     * @param passportId Connected passport id.
     * @param studyId Connected study id.
     */
    public void createAuditLogBookEntries(String passportId, String studyId) {
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
    private boolean isRelatedToPassport(AuditLog auditLog, String studyId) {
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
     * Creates and saves a new AuditLog entry, attributed to whoever the access token belongs to -
     * an interactive user or, for an automated integration, the SoftwareAgent bound to the
     * Keycloak service account the token was issued to.
     *
     * @param principal         Access token of the caller that performed the action.
     * @param actionType        "CREATE", "UPDATE", or "DELETE", etc.
     * @param affectedRelation  The table/collection name (e.g. "Algorithm").
     * @param recordId          The primary key ID of the affected record.
     * @param entity            The updated/created entity; can be null or a full object.
     * @return                  The saved AuditLog entity.
     */
    public AuditLog createAuditLog(
            Jwt principal,
            String studyId,
            Operation actionType,
            String affectedRelation,
            String recordId,
            Object entity
    ) {
        String description = switch (actionType) {
            case CREATE -> Description.CREATION.getDescription(affectedRelation, recordId);
            case UPDATE -> Description.UPDATE.getDescription(affectedRelation, recordId);
            case DELETE -> Description.DELETION.getDescription(affectedRelation, recordId);
            default -> null;
        };

        String recordData = (entity != null)
                ? JSONUtil.objectToJsonSafely(entity)
                : "None";

        AuditLog auditLog = new AuditLog();
        applyActor(auditLog, principal);
        auditLog.setStudyId(studyId);
        auditLog.setActionType(actionType.name());
        auditLog.setAffectedRelation(affectedRelation);
        auditLog.setAffectedRecordId(recordId);
        auditLog.setAffectedRecord(recordData);
        auditLog.setDescription(description);
        auditLog.setOccurredAt(Instant.now());

        return auditLogRepository.save(auditLog);
    }

    /**
     * Attributes the log entry to the caller. A Keycloak service account has no interactive user
     * behind it - its {@code preferred_username} is {@code service-account-<clientId>} - so the
     * token's authorized party is resolved to the SoftwareAgent registered for that client.
     *
     * @param auditLog  The entry being written.
     * @param principal Access token of the caller.
     */
    private void applyActor(AuditLog auditLog, Jwt principal) {
        String username = principal.getClaim(TokenClaim.USERNAME.getValue());
        String clientId = principal.getClaim(TokenClaim.AUTHORIZED_PARTY.getValue());

        if (username != null && username.startsWith(SERVICE_ACCOUNT_USERNAME_PREFIX)) {
            SoftwareAgent agent = softwareAgentService.findSoftwareAgentByKeycloakClientId(clientId).orElse(null);
            auditLog.setActorType(ActorType.SOFTWARE_AGENT);
            auditLog.setActorId(agent != null ? agent.getSoftwareAgentId() : clientId);
            auditLog.setActorName(agent != null ? agent.getName() : username);
        } else {
            auditLog.setActorType(ActorType.PERSONNEL);
            auditLog.setActorId(principal.getSubject());
            auditLog.setActorName(username);
        }
    }
}
