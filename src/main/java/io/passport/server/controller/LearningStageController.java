package io.passport.server.controller;

import io.passport.server.model.LearningStage;
import io.passport.server.model.Role;
import io.passport.server.service.LearningStageService;
import io.passport.server.service.RoleCheckerService;
import org.keycloak.KeycloakPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final LearningStageService learningStageService;
    private final RoleCheckerService roleCheckerService;
    private final List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);

    @Autowired
    public LearningStageController(LearningStageService learningStageService, RoleCheckerService roleCheckerService) {
        this.learningStageService = learningStageService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Retrieves learning stages, optionally filtered by the learningProcessId.
     * @param studyId ID of the study for authorization
     * @param learningProcessId the ID of the learning process (optional)
     * @param principal KeycloakPrincipal object that holds access token
     * @return a list of learning stages
     */
    @GetMapping
    public ResponseEntity<List<LearningStage>> getLearningStages(
            @RequestParam Long studyId,
            @RequestParam(required = false) Long learningProcessId,
            @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<LearningStage> learningStages = learningProcessId != null
                ? learningStageService.findLearningStagesByProcessId(learningProcessId)
                : learningStageService.getAllLearningStages();

        return ResponseEntity.ok(learningStages);
    }

    /**
     * Read a learning stage by id
     * @param studyId ID of the study for authorization
     * @param learningStageId ID of the learning stage
     * @param principal KeycloakPrincipal object that holds access token
     * @return ResponseEntity with LearningStage data or not found status
     */
    @GetMapping("/{learningStageId}")
    public ResponseEntity<?> getLearningStage(@RequestParam Long studyId,
                                              @PathVariable Long learningStageId,
                                              @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<LearningStage> learningStage = this.learningStageService.findLearningStageById(learningStageId);
        return learningStage.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create LearningStage.
     * @param studyId ID of the study for authorization
     * @param learningStage LearningStage model instance to be created
     * @param principal KeycloakPrincipal object that holds access token
     * @return ResponseEntity with the created LearningStage
     */
    @PostMapping()
    public ResponseEntity<?> createLearningStage(@RequestParam Long studyId,
                                                 @RequestBody LearningStage learningStage,
                                                 @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            LearningStage savedLearningStage = this.learningStageService.saveLearningStage(learningStage);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedLearningStage);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update LearningStage.
     * @param studyId ID of the study for authorization
     * @param learningStageId ID of the learning stage that is to be updated
     * @param updatedLearningStage LearningStage model instance with updated details
     * @param principal KeycloakPrincipal object that holds access token
     * @return ResponseEntity with the updated LearningStage or not found status
     */
    @PutMapping("/{learningStageId}")
    public ResponseEntity<?> updateLearningStage(@RequestParam Long studyId,
                                                 @PathVariable Long learningStageId,
                                                 @RequestBody LearningStage updatedLearningStage,
                                                 @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<LearningStage> savedLearningStage = this.learningStageService.updateLearningStage(learningStageId, updatedLearningStage);
            return savedLearningStage.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete LearningStage by ID.
     * @param studyId ID of the study for authorization
     * @param learningStageId ID of the learning stage that is to be deleted
     * @param principal KeycloakPrincipal object that holds access token
     * @return ResponseEntity with no content status or not found status
     */
    @DeleteMapping("/{learningStageId}")
    public ResponseEntity<?> deleteLearningStage(@RequestParam Long studyId,
                                                 @PathVariable Long learningStageId,
                                                 @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.learningStageService.deleteLearningStage(learningStageId);
            return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
