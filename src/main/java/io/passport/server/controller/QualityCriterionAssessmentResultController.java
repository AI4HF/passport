package io.passport.server.controller;

import io.passport.server.model.QualityCriterionAssessmentResult;
import io.passport.server.model.Operation;
import io.passport.server.model.Role;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.RoleCheckerService;
import io.passport.server.service.QualityCriterionAssessmentResultService;
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
 * Class which stores the generated HTTP requests related to QualityCriterionAssessmentResult operations.
 */
@RestController
@RequestMapping("/quality-criterion-assessment-result")
public class QualityCriterionAssessmentResultController {

    private static final Logger log = LoggerFactory.getLogger(QualityCriterionAssessmentResultController.class);

    private final String relationName = "QualityCriterionAssessmentResult";
    private final QualityCriterionAssessmentResultService resultService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);

    @Autowired
    public QualityCriterionAssessmentResultController(QualityCriterionAssessmentResultService resultService,
                              RoleCheckerService roleCheckerService,
                              AuditLogBookService auditLogBookService) {
        this.resultService = resultService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Reads the per-criterion results of an assessment run.
     *
     * @param qualityAssessmentId ID of the assessment run
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return List of QualityCriterionAssessmentResult
     */
    @GetMapping
    public ResponseEntity<List<QualityCriterionAssessmentResult>> getAll(@RequestParam(required = true) String qualityAssessmentId,
                                                 @RequestParam String studyId,
                                                 @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<QualityCriterionAssessmentResult> items = this.resultService.findResultsByQualityAssessmentId(qualityAssessmentId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(items.size()));
        return ResponseEntity.ok().headers(headers).body(items);
    }

    /**
     * Reads a single QualityCriterionAssessmentResult by its id.
     *
     * @param resultId ID of the QualityCriterionAssessmentResult
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return QualityCriterionAssessmentResult or NOT_FOUND
     */
    @GetMapping("/{resultId}")
    public ResponseEntity<?> getById(@PathVariable String resultId,
                                     @RequestParam String studyId,
                                     @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<QualityCriterionAssessmentResult> item = this.resultService.findResultById(resultId);
        return item.<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new QualityCriterionAssessmentResult.
     *
     * @param item      The QualityCriterionAssessmentResult to create
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Created QualityCriterionAssessmentResult or BAD_REQUEST on error
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody QualityCriterionAssessmentResult item,
                                    @RequestParam String studyId,
                                    @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            QualityCriterionAssessmentResult saved = this.resultService.saveResult(item);
            if (saved.getResultId() != null) {
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.CREATE,
                        relationName,
                        saved.getResultId(),
                        saved
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("Error creating QualityCriterionAssessmentResult: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Updates an existing QualityCriterionAssessmentResult.
     *
     * @param resultId ID of the QualityCriterionAssessmentResult to update
     * @param item      Updated QualityCriterionAssessmentResult data
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Updated QualityCriterionAssessmentResult or NOT_FOUND
     */
    @PutMapping("/{resultId}")
    public ResponseEntity<?> update(@PathVariable String resultId,
                                    @RequestBody QualityCriterionAssessmentResult item,
                                    @RequestParam String studyId,
                                    @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<QualityCriterionAssessmentResult> savedOpt = this.resultService.updateResult(resultId, item);
            if (savedOpt.isPresent()) {
                QualityCriterionAssessmentResult saved = savedOpt.get();
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.UPDATE,
                        relationName,
                        saved.getResultId(),
                        saved
                );
                return ResponseEntity.ok().body(saved);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating QualityCriterionAssessmentResult: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Deletes a QualityCriterionAssessmentResult.
     *
     * @param resultId ID of the QualityCriterionAssessmentResult to delete
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return NO_CONTENT or NOT_FOUND
     */
    @DeleteMapping("/{resultId}")
    public ResponseEntity<?> delete(@PathVariable String resultId,
                                    @RequestParam String studyId,
                                    @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.resultService.deleteResult(resultId);
            if (isDeleted) {
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.DELETE,
                        relationName,
                        resultId,
                        null
                );
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting QualityCriterionAssessmentResult: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
