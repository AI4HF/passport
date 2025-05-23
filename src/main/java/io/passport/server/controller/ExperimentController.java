package io.passport.server.controller;

import io.passport.server.model.*;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.ExperimentService;
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

/**
 * Class which stores the generated HTTP requests related to experiment operations.
 */
@RestController
@RequestMapping("/experiment")
public class ExperimentController {

    private static final Logger log = LoggerFactory.getLogger(ExperimentController.class);

    private final String relationName = "Experiment";
    private final ExperimentService experimentService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER, Role.STUDY_OWNER, Role.DATA_SCIENTIST);

    @Autowired
    public ExperimentController(ExperimentService experimentService,
                                RoleCheckerService roleCheckerService,
                                AuditLogBookService auditLogBookService) {
        this.experimentService = experimentService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Retrieves all experiments for the given study ID.
     *
     * @param studyId   ID of the study
     * @param principal Jwt principal containing user info
     * @return List of experiments or FORBIDDEN if not authorized
     */
    @GetMapping
    public ResponseEntity<List<Experiment>> getExperimentsByStudyId(@RequestParam(value = "studyId") String studyId,
                                                                    @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Experiment> experiment = this.experimentService.findExperimentByStudyId(studyId);
        return ResponseEntity.ok().body(experiment);
    }

    /**
     * Creates multiple Experiment entries for a given study.
     *
     * @param studyId     ID of the study for authorization
     * @param experiments List of Experiment objects to create
     * @param principal   Jwt principal containing user info
     * @return List of newly created experiments
     */
    @PostMapping
    public ResponseEntity<?> createExperiments(@RequestParam String studyId,
                                               @RequestBody List<Experiment> experiments,
                                               @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            List<Experiment> newExperiments = this.experimentService.createExperimentEntries(studyId, experiments);

            String userId = principal.getSubject();
            for (Experiment exp : newExperiments) {
                String recordId = exp.getExperimentId();
                auditLogBookService.createAuditLog(
                        userId,
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.CREATE,
                        relationName,
                        recordId,
                        exp
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(newExperiments);
        } catch (Exception e) {
            log.error("Error creating Experiments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
