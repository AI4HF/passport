package io.passport.server.controller;

import io.passport.server.model.QualityCriteria;
import io.passport.server.model.Operation;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.RoleCheckerService;
import io.passport.server.service.QualityCriteriaService;
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
 * Class which stores the generated HTTP requests related to QualityCriteria operations.
 */
@RestController
@RequestMapping("/quality-criteria")
public class QualityCriteriaController {

    private static final Logger log = LoggerFactory.getLogger(QualityCriteriaController.class);

    private final String relationName = "QualityCriteria";
    private final QualityCriteriaService qualityCriteriaService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);

    @Autowired
    public QualityCriteriaController(QualityCriteriaService qualityCriteriaService,
                              RoleCheckerService roleCheckerService,
                              AuditLogBookService auditLogBookService) {
        this.qualityCriteriaService = qualityCriteriaService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Validates if a QualityCriteria deletion is safe and authorized.
     *
     * @param qualityCriteriaId ID of the QualityCriteria being deleted
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Comma separated list of cascaded entries
     */
    @GetMapping("/{qualityCriteriaId}/validate-deletion")
    public ResponseEntity<String> validateDeletion(@PathVariable String qualityCriteriaId,
                                                   @RequestParam String studyId,
                                                   @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("QualityCriteria");
        }

        ValidationResult result = qualityCriteriaService.validateQualityCriteriaDeletion(studyId, qualityCriteriaId, principal);

        if (result.status()) {
            return ResponseEntity.ok(result.tables());
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result.tables());
        }
    }

    /**
     * Reads the quality criteria sets of a study, optionally filtered to one experiment.
     *
     * @param experimentId Optional experiment filter
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return List of QualityCriteria
     */
    @GetMapping
    public ResponseEntity<List<QualityCriteria>> getAll(@RequestParam(required = false) String experimentId,
                                                 @RequestParam String studyId,
                                                 @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<QualityCriteria> items = experimentId != null
                ? this.qualityCriteriaService.findQualityCriteriaByExperimentId(experimentId)
                : this.qualityCriteriaService.getAllQualityCriteriaByStudyId(studyId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(items.size()));
        return ResponseEntity.ok().headers(headers).body(items);
    }

    /**
     * Reads a single QualityCriteria by its id.
     *
     * @param qualityCriteriaId ID of the QualityCriteria
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return QualityCriteria or NOT_FOUND
     */
    @GetMapping("/{qualityCriteriaId}")
    public ResponseEntity<?> getById(@PathVariable String qualityCriteriaId,
                                     @RequestParam String studyId,
                                     @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<QualityCriteria> item = this.qualityCriteriaService.findQualityCriteriaById(qualityCriteriaId);
        return item.<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new QualityCriteria.
     *
     * @param item      The QualityCriteria to create
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Created QualityCriteria or BAD_REQUEST on error
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody QualityCriteria item,
                                    @RequestParam String studyId,
                                    @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            QualityCriteria saved = this.qualityCriteriaService.saveQualityCriteria(item);
            if (saved.getQualityCriteriaId() != null) {
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.CREATE,
                        relationName,
                        saved.getQualityCriteriaId(),
                        saved
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("Error creating QualityCriteria: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Updates an existing QualityCriteria.
     *
     * @param qualityCriteriaId ID of the QualityCriteria to update
     * @param item      Updated QualityCriteria data
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Updated QualityCriteria or NOT_FOUND
     */
    @PutMapping("/{qualityCriteriaId}")
    public ResponseEntity<?> update(@PathVariable String qualityCriteriaId,
                                    @RequestBody QualityCriteria item,
                                    @RequestParam String studyId,
                                    @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<QualityCriteria> savedOpt = this.qualityCriteriaService.updateQualityCriteria(qualityCriteriaId, item);
            if (savedOpt.isPresent()) {
                QualityCriteria saved = savedOpt.get();
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.UPDATE,
                        relationName,
                        saved.getQualityCriteriaId(),
                        saved
                );
                return ResponseEntity.ok().body(saved);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating QualityCriteria: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Deletes a QualityCriteria.
     *
     * @param qualityCriteriaId ID of the QualityCriteria to delete
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return NO_CONTENT or NOT_FOUND
     */
    @DeleteMapping("/{qualityCriteriaId}")
    public ResponseEntity<?> delete(@PathVariable String qualityCriteriaId,
                                    @RequestParam String studyId,
                                    @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.qualityCriteriaService.deleteQualityCriteria(qualityCriteriaId);
            if (isDeleted) {
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.DELETE,
                        relationName,
                        qualityCriteriaId,
                        null
                );
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting QualityCriteria: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
