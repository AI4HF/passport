package io.passport.server.controller;

import io.passport.server.model.*;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.LearningProcessService;
import io.passport.server.service.RoleCheckerService;
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
 * Class which stores the generated HTTP requests related to learning process operations.
 */
@RestController
@RequestMapping("/learning-process")
public class LearningProcessController {

    private static final Logger log = LoggerFactory.getLogger(LearningProcessController.class);

    private final String relationName = "Learning Process";
    private final LearningProcessService learningProcessService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    private final List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);

    @Autowired
    public LearningProcessController(LearningProcessService learningProcessService,
                                     RoleCheckerService roleCheckerService,
                                     AuditLogBookService auditLogBookService) {
        this.learningProcessService = learningProcessService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Reads all learning processes by a given studyId.
     *
     * @param studyId   ID of the study
     * @param principal Jwt principal containing user info
     * @return List of LearningProcess objects
     */
    @GetMapping
    public ResponseEntity<List<LearningProcess>> getAllLearningProcessesByStudyId(@RequestParam Long studyId,
                                                                                  @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<LearningProcess> learningProcesses = this.learningProcessService.getAllLearningProcessByStudyId(studyId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(learningProcesses.size()));
        return ResponseEntity.ok().headers(headers).body(learningProcesses);
    }

    /**
     * Reads a single LearningProcess by its ID.
     *
     * @param studyId           ID of the study for authorization
     * @param learningProcessId ID of the LearningProcess
     * @param principal         Jwt principal containing user info
     * @return The LearningProcess or NOT_FOUND
     */
    @GetMapping("/{learningProcessId}")
    public ResponseEntity<?> getLearningProcess(@RequestParam Long studyId,
                                                @PathVariable Long learningProcessId,
                                                @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<LearningProcess> lpOpt = this.learningProcessService.findLearningProcessById(learningProcessId);
        return lpOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new LearningProcess.
     *
     * @param studyId         ID of the study for authorization
     * @param learningProcess LearningProcess model to create
     * @param principal       Jwt principal containing user info
     * @return Created LearningProcess or BAD_REQUEST on error
     */
    @PostMapping
    public ResponseEntity<?> createLearningProcess(@RequestParam Long studyId,
                                                   @RequestBody LearningProcess learningProcess,
                                                   @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            LearningProcess saved = this.learningProcessService.saveLearningProcess(learningProcess);
            if (saved.getLearningProcessId() != null) {
                String recordId = saved.getLearningProcessId().toString();
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
            log.error("Error creating LearningProcess: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Updates an existing LearningProcess by learningProcessId.
     *
     * @param studyId                ID of the study for authorization
     * @param learningProcessId      ID of the LearningProcess to update
     * @param updatedLearningProcess Updated details
     * @param principal              Jwt principal containing user info
     * @return Updated LearningProcess or NOT_FOUND
     */
    @PutMapping("/{learningProcessId}")
    public ResponseEntity<?> updateLearningProcess(@RequestParam Long studyId,
                                                   @PathVariable Long learningProcessId,
                                                   @RequestBody LearningProcess updatedLearningProcess,
                                                   @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<LearningProcess> savedOpt =
                    this.learningProcessService.updateLearningProcess(learningProcessId, updatedLearningProcess);

            if (savedOpt.isPresent()) {
                LearningProcess saved = savedOpt.get();
                String recordId = saved.getLearningProcessId().toString();
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
            log.error("Error updating LearningProcess: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Deletes a LearningProcess by its ID.
     *
     * @param studyId           ID of the study for authorization
     * @param learningProcessId ID of the LearningProcess to delete
     * @param principal         Jwt principal containing user info
     * @return OK if deleted, NOT_FOUND otherwise
     */
    @DeleteMapping("/{learningProcessId}")
    public ResponseEntity<?> deleteLearningProcess(@RequestParam Long studyId,
                                                   @PathVariable Long learningProcessId,
                                                   @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<LearningProcess> deletedLearningProcess = this.learningProcessService.deleteLearningProcess(learningProcessId);
            if (deletedLearningProcess.isPresent()) {
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.DELETE,
                        relationName,
                        learningProcessId.toString(),
                        deletedLearningProcess.get()
                );
                return ResponseEntity.status(HttpStatus.OK).body(deletedLearningProcess.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting LearningProcess: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
