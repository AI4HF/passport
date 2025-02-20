package io.passport.server.controller;

import io.passport.server.model.Experiment;
import io.passport.server.model.Role;
import io.passport.server.service.ExperimentService;
import io.passport.server.service.RoleCheckerService;
import org.keycloak.KeycloakPrincipal;
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

    private final ExperimentService experimentService;
    private final RoleCheckerService roleCheckerService;

    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER, Role.STUDY_OWNER);

    @Autowired
    public ExperimentController(ExperimentService experimentService, RoleCheckerService roleCheckerService) {
        this.experimentService = experimentService;
        this.roleCheckerService = roleCheckerService;
    }

    @GetMapping()
    public ResponseEntity<List<Experiment>> getExperimentsByStudyId(@RequestParam(value = "studyId") Long studyId,
                                                                    @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Experiment> experiment = this.experimentService.findExperimentByStudyId(studyId);
        return ResponseEntity.ok().body(experiment);
    }

    @PostMapping()
    public ResponseEntity<?> createExperiments(@RequestParam Long studyId,
                                               @RequestBody List<Experiment> experiments,
                                               @AuthenticationPrincipal Jwt principal) {
        // Check authorization using studyId
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<Experiment> newExperiments = this.experimentService.createExperimentEntries(studyId, experiments);
            return ResponseEntity.status(HttpStatus.CREATED).body(newExperiments);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

