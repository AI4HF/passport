package io.passport.server.controller;

import io.passport.server.model.QualityAssessment;
import io.passport.server.model.Operation;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.RoleCheckerService;
import io.passport.server.service.QualityAssessmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Class which stores the generated HTTP requests related to QualityAssessment operations.
 */
@RestController
@RequestMapping("/quality-assessment")
public class QualityAssessmentController {

    private static final Logger log = LoggerFactory.getLogger(QualityAssessmentController.class);

    private final String relationName = "QualityAssessment";
    private final QualityAssessmentService qualityAssessmentService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);

    @Autowired
    public QualityAssessmentController(QualityAssessmentService qualityAssessmentService,
                              RoleCheckerService roleCheckerService,
                              AuditLogBookService auditLogBookService) {
        this.qualityAssessmentService = qualityAssessmentService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Validates if a QualityAssessment deletion is safe and authorized.
     *
     * @param qualityAssessmentId ID of the QualityAssessment being deleted
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Comma separated list of cascaded entries
     */
    @GetMapping("/{qualityAssessmentId}/validate-deletion")
    public ResponseEntity<String> validateDeletion(@PathVariable String qualityAssessmentId,
                                                   @RequestParam String studyId,
                                                   @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("QualityAssessment");
        }

        ValidationResult result = qualityAssessmentService.validateQualityAssessmentDeletion(studyId, qualityAssessmentId, principal);

        if (result.status()) {
            return ResponseEntity.ok(result.tables());
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result.tables());
        }
    }

    /**
     * Reads the quality assessment runs of a study, optionally filtered to one dataset.
     *
     * @param datasetId Optional dataset filter
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return List of QualityAssessment
     */
    @GetMapping
    public ResponseEntity<List<QualityAssessment>> getAll(@RequestParam(required = false) String datasetId,
                                                 @RequestParam String studyId,
                                                 @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<QualityAssessment> items = datasetId != null
                ? this.qualityAssessmentService.findQualityAssessmentsByDatasetId(datasetId)
                : this.qualityAssessmentService.getAllQualityAssessmentsByStudyId(studyId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(items.size()));
        return ResponseEntity.ok().headers(headers).body(items);
    }

    /**
     * Reads a single QualityAssessment by its id.
     *
     * @param qualityAssessmentId ID of the QualityAssessment
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return QualityAssessment or NOT_FOUND
     */
    @GetMapping("/{qualityAssessmentId}")
    public ResponseEntity<?> getById(@PathVariable String qualityAssessmentId,
                                     @RequestParam String studyId,
                                     @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<QualityAssessment> item = this.qualityAssessmentService.findQualityAssessmentById(qualityAssessmentId);
        return item.<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new QualityAssessment.
     *
     * @param item      The QualityAssessment to create
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Created QualityAssessment or BAD_REQUEST on error
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody QualityAssessment item,
                                    @RequestParam String studyId,
                                    @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            QualityAssessment saved = this.qualityAssessmentService.saveQualityAssessment(item);
            if (saved.getQualityAssessmentId() != null) {
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.CREATE,
                        relationName,
                        saved.getQualityAssessmentId(),
                        saved
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("Error creating QualityAssessment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Updates an existing QualityAssessment.
     *
     * @param qualityAssessmentId ID of the QualityAssessment to update
     * @param item      Updated QualityAssessment data
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Updated QualityAssessment or NOT_FOUND
     */
    @PutMapping("/{qualityAssessmentId}")
    public ResponseEntity<?> update(@PathVariable String qualityAssessmentId,
                                    @RequestBody QualityAssessment item,
                                    @RequestParam String studyId,
                                    @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<QualityAssessment> savedOpt = this.qualityAssessmentService.updateQualityAssessment(qualityAssessmentId, item);
            if (savedOpt.isPresent()) {
                QualityAssessment saved = savedOpt.get();
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.UPDATE,
                        relationName,
                        saved.getQualityAssessmentId(),
                        saved
                );
                return ResponseEntity.ok().body(saved);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating QualityAssessment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Deletes a QualityAssessment.
     *
     * @param qualityAssessmentId ID of the QualityAssessment to delete
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return NO_CONTENT or NOT_FOUND
     */
    @DeleteMapping("/{qualityAssessmentId}")
    public ResponseEntity<?> delete(@PathVariable String qualityAssessmentId,
                                    @RequestParam String studyId,
                                    @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.qualityAssessmentService.deleteQualityAssessment(qualityAssessmentId);
            if (isDeleted) {
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.DELETE,
                        relationName,
                        qualityAssessmentId,
                        null
                );
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting QualityAssessment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
