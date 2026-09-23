package io.passport.server.controller;

import io.passport.server.model.QualityCriterion;
import io.passport.server.model.Operation;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.RoleCheckerService;
import io.passport.server.service.QualityCriterionService;
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
 * Class which stores the generated HTTP requests related to QualityCriterion operations.
 */
@RestController
@RequestMapping("/quality-criterion")
public class QualityCriterionController {

    private static final Logger log = LoggerFactory.getLogger(QualityCriterionController.class);

    private final String relationName = "QualityCriterion";
    private final QualityCriterionService qualityCriterionService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);

    @Autowired
    public QualityCriterionController(QualityCriterionService qualityCriterionService,
                              RoleCheckerService roleCheckerService,
                              AuditLogBookService auditLogBookService) {
        this.qualityCriterionService = qualityCriterionService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Validates if a QualityCriterion deletion is safe and authorized.
     *
     * @param qualityCriterionId ID of the QualityCriterion being deleted
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Comma separated list of cascaded entries
     */
    @GetMapping("/{qualityCriterionId}/validate-deletion")
    public ResponseEntity<String> validateDeletion(@PathVariable String qualityCriterionId,
                                                   @RequestParam String studyId,
                                                   @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("QualityCriterion");
        }

        ValidationResult result = qualityCriterionService.validateQualityCriterionDeletion(studyId, qualityCriterionId, principal);

        if (result.status()) {
            return ResponseEntity.ok(result.tables());
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result.tables());
        }
    }

    /**
     * Reads the rules of a quality criteria set.
     *
     * @param qualityCriteriaId ID of the owning criteria set
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return List of QualityCriterion
     */
    @GetMapping
    public ResponseEntity<List<QualityCriterion>> getAll(@RequestParam(required = true) String qualityCriteriaId,
                                                 @RequestParam String studyId,
                                                 @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<QualityCriterion> items = this.qualityCriterionService.findQualityCriterionByQualityCriteriaId(qualityCriteriaId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(items.size()));
        return ResponseEntity.ok().headers(headers).body(items);
    }

    /**
     * Reads a single QualityCriterion by its id.
     *
     * @param qualityCriterionId ID of the QualityCriterion
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return QualityCriterion or NOT_FOUND
     */
    @GetMapping("/{qualityCriterionId}")
    public ResponseEntity<?> getById(@PathVariable String qualityCriterionId,
                                     @RequestParam String studyId,
                                     @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<QualityCriterion> item = this.qualityCriterionService.findQualityCriterionById(qualityCriterionId);
        return item.<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new QualityCriterion.
     *
     * @param item      The QualityCriterion to create
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Created QualityCriterion or BAD_REQUEST on error
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody QualityCriterion item,
                                    @RequestParam String studyId,
                                    @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            QualityCriterion saved = this.qualityCriterionService.saveQualityCriterion(item);
            if (saved.getQualityCriterionId() != null) {
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.CREATE,
                        relationName,
                        saved.getQualityCriterionId(),
                        saved
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("Error creating QualityCriterion: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Updates an existing QualityCriterion.
     *
     * @param qualityCriterionId ID of the QualityCriterion to update
     * @param item      Updated QualityCriterion data
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Updated QualityCriterion or NOT_FOUND
     */
    @PutMapping("/{qualityCriterionId}")
    public ResponseEntity<?> update(@PathVariable String qualityCriterionId,
                                    @RequestBody QualityCriterion item,
                                    @RequestParam String studyId,
                                    @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<QualityCriterion> savedOpt = this.qualityCriterionService.updateQualityCriterion(qualityCriterionId, item);
            if (savedOpt.isPresent()) {
                QualityCriterion saved = savedOpt.get();
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.UPDATE,
                        relationName,
                        saved.getQualityCriterionId(),
                        saved
                );
                return ResponseEntity.ok().body(saved);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating QualityCriterion: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Deletes a QualityCriterion.
     *
     * @param qualityCriterionId ID of the QualityCriterion to delete
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return NO_CONTENT or NOT_FOUND
     */
    @DeleteMapping("/{qualityCriterionId}")
    public ResponseEntity<?> delete(@PathVariable String qualityCriterionId,
                                    @RequestParam String studyId,
                                    @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.qualityCriterionService.deleteQualityCriterion(qualityCriterionId);
            if (isDeleted) {
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.DELETE,
                        relationName,
                        qualityCriterionId,
                        null
                );
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting QualityCriterion: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
