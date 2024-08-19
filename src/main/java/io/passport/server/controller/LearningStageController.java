package io.passport.server.controller;

import io.passport.server.model.LearningStage;
import io.passport.server.model.Role;
import io.passport.server.service.LearningStageService;
import io.passport.server.service.RoleCheckerService;
import org.keycloak.KeycloakPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
    /**
     * LearningStage service for learning stage management
     */
    private final LearningStageService learningStageService;

    /**
     * Role checker service for authorization
     */
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public LearningStageController(LearningStageService learningStageService, RoleCheckerService roleCheckerService) {
        this.learningStageService = learningStageService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Retrieves learning stages. If a learningProcessId is provided, it filters by that process ID; otherwise, it retrieves all learning stages.
     * @param learningProcessId the ID of the learning process (optional)
     * @param principal KeycloakPrincipal object that holds access token
     * @return a list of learning stages
     */
    @GetMapping
    public ResponseEntity<List<LearningStage>> getLearningStages(
            @RequestParam(required = false) Long learningProcessId,
            @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<LearningStage> learningStages;

        if (learningProcessId != null) {
            learningStages = learningStageService.findLearningStagesByProcessId(learningProcessId);
        } else {
            learningStages = learningStageService.getAllLearningStages();
        }

        return ResponseEntity.ok(learningStages);
    }


    /**
     * Read a learning stage by id
     * @param learningStageId ID of the learning stage
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping("/{learningStageId}")
    public ResponseEntity<?> getLearningStage(@PathVariable Long learningStageId,
                                              @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<LearningStage> learningStage = this.learningStageService.findLearningStageById(learningStageId);

        if(learningStage.isPresent()) {
            return ResponseEntity.ok().body(learningStage.get());
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create LearningStage.
     * @param learningStage LearningStage model instance to be created.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createLearningStage(@RequestBody LearningStage learningStage,
                                                 @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            LearningStage savedLearningStage = this.learningStageService.saveLearningStage(learningStage);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedLearningStage);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update LearningStage.
     * @param learningStageId ID of the learning stage that is to be updated.
     * @param updatedLearningStage LearningStage model instance with updated details.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PutMapping("/{learningStageId}")
    public ResponseEntity<?> updateLearningStage(@PathVariable Long learningStageId,
                                                 @RequestBody LearningStage updatedLearningStage,
                                                 @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<LearningStage> savedLearningStage = this.learningStageService.updateLearningStage(learningStageId, updatedLearningStage);
            if(savedLearningStage.isPresent()) {
                return ResponseEntity.ok().body(savedLearningStage);
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by LearningStage ID.
     * @param learningStageId ID of the learning stage that is to be deleted.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @DeleteMapping("/{learningStageId}")
    public ResponseEntity<?> deleteLearningStage(@PathVariable Long learningStageId,
                                                 @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.learningStageService.deleteLearningStage(learningStageId);
            if(isDeleted) {
                return ResponseEntity.noContent().build();
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
