package io.passport.server.controller;

import io.passport.server.model.*;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.LearningStageService;
import io.passport.server.service.RoleCheckerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Class which stores the generated HTTP requests related to learning stage operations.
 */
@RestController
@RequestMapping("/learning-stage")
public class LearningStageController {

    private static final Logger log = LoggerFactory.getLogger(LearningStageController.class);

    private final String relationName = "Learning Stage";
    private final LearningStageService learningStageService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    private final List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);

    @Autowired
    public LearningStageController(LearningStageService learningStageService,
                                   RoleCheckerService roleCheckerService,
                                   AuditLogBookService auditLogBookService) {
        this.learningStageService = learningStageService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Retrieves learning stages, optionally filtered by the learningProcessId.
     *
     * @param studyId           ID of the study for authorization
     * @param learningProcessId Optional ID of the learning process to filter
     * @param principal         Jwt principal containing user info
     * @return List of LearningStages
     */
    @GetMapping
    public ResponseEntity<List<LearningStage>> getLearningStages(
            @RequestParam Long studyId,
            @RequestParam(required = false) Long learningProcessId,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<LearningStage> learningStages = (learningProcessId != null)
                ? learningStageService.findLearningStagesByProcessId(learningProcessId)
                : learningStageService.getAllLearningStages();

        return ResponseEntity.ok(learningStages);
    }

    /**
     * Retrieves a single LearningStage by its ID.
     *
     * @param studyId         ID of the study for authorization
     * @param learningStageId ID of the LearningStage
     * @param principal       Jwt principal containing user info
     * @return The LearningStage or NOT_FOUND
     */
    @GetMapping("/{learningStageId}")
    public ResponseEntity<?> getLearningStage(@RequestParam Long studyId,
                                              @PathVariable Long learningStageId,
                                              @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<LearningStage> lsOpt = this.learningStageService.findLearningStageById(learningStageId);
        return lsOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new LearningStage.
     *
     * @param studyId       ID of the study for authorization
     * @param learningStage LearningStage model to create
     * @param principal     Jwt principal containing user info
     * @return Created LearningStage or BAD_REQUEST on error
     */
    @PostMapping
    public ResponseEntity<?> createLearningStage(@RequestParam Long studyId,
                                                 @RequestBody LearningStage learningStage,
                                                 @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            LearningStage saved = this.learningStageService.saveLearningStage(learningStage);
            if (saved.getLearningStageId() != null) {
                String recordId = saved.getLearningStageId().toString();
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.CREATE,
                        relationName,
                        recordId,
                        saved
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {
            log.error("Error creating LearningStage: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Updates an existing LearningStage by learningStageId.
     *
     * @param studyId              ID of the study for authorization
     * @param learningStageId      ID of the LearningStage to update
     * @param updatedLearningStage Updated details
     * @param principal            Jwt principal containing user info
     * @return Updated LearningStage or NOT_FOUND
     */
    @PutMapping("/{learningStageId}")
    public ResponseEntity<?> updateLearningStage(@RequestParam Long studyId,
                                                 @PathVariable Long learningStageId,
                                                 @RequestBody LearningStage updatedLearningStage,
                                                 @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<LearningStage> savedOpt = this.learningStageService.updateLearningStage(learningStageId, updatedLearningStage);
            if (savedOpt.isPresent()) {
                LearningStage saved = savedOpt.get();
                String recordId = saved.getLearningStageId().toString();
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.UPDATE,
                        relationName,
                        recordId,
                        saved
                );
                return ResponseEntity.ok(saved);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error updating LearningStage: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Deletes a LearningStage by its ID.
     *
     * @param studyId         ID of the study for authorization
     * @param learningStageId ID of the LearningStage to delete
     * @param principal       Jwt principal containing user info
     * @return OK if deleted, NOT_FOUND otherwise
     */
    @DeleteMapping("/{learningStageId}")
    public ResponseEntity<?> deleteLearningStage(@RequestParam Long studyId,
                                                 @PathVariable Long learningStageId,
                                                 @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<LearningStage> deletedLearningStage = this.learningStageService.deleteLearningStage(learningStageId);
            if (deletedLearningStage.isPresent()) {
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.DELETE,
                        relationName,
                        learningStageId.toString(),
                        deletedLearningStage.get()
                );
                return ResponseEntity.status(HttpStatus.OK).body(deletedLearningStage.get());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error deleting LearningStage: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
