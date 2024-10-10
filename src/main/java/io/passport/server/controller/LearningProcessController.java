package io.passport.server.controller;

import io.passport.server.model.LearningProcess;
import io.passport.server.model.Role;
import io.passport.server.service.LearningProcessService;
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
 * Class which stores the generated HTTP requests related to learning process operations.
 */
@RestController
@RequestMapping("/learning-process")
public class LearningProcessController {
    private static final Logger log = LoggerFactory.getLogger(LearningProcessController.class);
    private final LearningProcessService learningProcessService;
    private final RoleCheckerService roleCheckerService;
    private final List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);

    @Autowired
    public LearningProcessController(LearningProcessService learningProcessService, RoleCheckerService roleCheckerService) {
        this.learningProcessService = learningProcessService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read all learning processes by studyId
     * @param studyId ID of the study for authorization
     * @param principal KeycloakPrincipal object that holds access token
     * @return ResponseEntity with list of LearningProcesses
     */
    @GetMapping()
    public ResponseEntity<List<LearningProcess>> getAllLearningProcessesByStudyId(@RequestParam Long studyId,
                                                                                  @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<LearningProcess> learningProcesses = this.learningProcessService.getAllLearningProcessByStudyId(studyId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(learningProcesses.size()));

        return ResponseEntity.ok().headers(headers).body(learningProcesses);
    }

    /**
     * Read a learning process by id
     * @param studyId ID of the study for authorization
     * @param learningProcessId ID of the learning process
     * @param principal KeycloakPrincipal object that holds access token
     * @return ResponseEntity with LearningProcess
     */
    @GetMapping("/{learningProcessId}")
    public ResponseEntity<?> getLearningProcess(@RequestParam Long studyId,
                                                @PathVariable Long learningProcessId,
                                                @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<LearningProcess> learningProcess = this.learningProcessService.findLearningProcessById(learningProcessId);
        return learningProcess.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create LearningProcess.
     * @param studyId ID of the study for authorization
     * @param learningProcess LearningProcess model instance to be created
     * @param principal KeycloakPrincipal object that holds access token
     * @return ResponseEntity with created LearningProcess
     */
    @PostMapping()
    public ResponseEntity<?> createLearningProcess(@RequestParam Long studyId,
                                                   @RequestBody LearningProcess learningProcess,
                                                   @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            LearningProcess savedLearningProcess = this.learningProcessService.saveLearningProcess(learningProcess);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedLearningProcess);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update LearningProcess.
     * @param studyId ID of the study for authorization
     * @param learningProcessId ID of the learning process that is to be updated
     * @param updatedLearningProcess LearningProcess model instance with updated details
     * @param principal KeycloakPrincipal object that holds access token
     * @return ResponseEntity with updated LearningProcess
     */
    @PutMapping("/{learningProcessId}")
    public ResponseEntity<?> updateLearningProcess(@RequestParam Long studyId,
                                                   @PathVariable Long learningProcessId,
                                                   @RequestBody LearningProcess updatedLearningProcess,
                                                   @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<LearningProcess> savedLearningProcess = this.learningProcessService.updateLearningProcess(learningProcessId, updatedLearningProcess);
            return savedLearningProcess.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by LearningProcess ID.
     * @param studyId ID of the study for authorization
     * @param learningProcessId ID of the learning process that is to be deleted
     * @param principal KeycloakPrincipal object that holds access token
     * @return ResponseEntity
     */
    @DeleteMapping("/{learningProcessId}")
    public ResponseEntity<?> deleteLearningProcess(@RequestParam Long studyId,
                                                   @PathVariable Long learningProcessId,
                                                   @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.learningProcessService.deleteLearningProcess(learningProcessId);
            return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
