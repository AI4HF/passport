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
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Class which stores the generated HTTP requests related to experiment operations.
 */
@RestController
@RequestMapping("/experiment")
public class ExperimentController {

    private static final Logger log = LoggerFactory.getLogger(ExperimentController.class);

    /**
     * Experiment service for experiment management
     */
    private final ExperimentService experimentService;

    /**
     * Role checker service for authorization
     */
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public ExperimentController(ExperimentService experimentService, RoleCheckerService roleCheckerService) {
        this.experimentService = experimentService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read experiments by studyId
     * Read all experiments if no studyId is provided
     * @param studyId ID of the study related to experiment.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<Experiment>> getExperimentsByStudyId(@RequestParam(value = "studyId", required = false) Long studyId,
                                                                    @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> allowedRoles = List.of(Role.DATA_ENGINEER, Role.STUDY_OWNER);
        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Experiment> experiment;
        if(studyId != null)
        {
            experiment = this.experimentService.findExperimentByStudyId(studyId);
        }
        else {
            String personnelId = this.roleCheckerService.getPersonnelId(principal);
            experiment = this.experimentService.findAllExperiments(personnelId);
        }

        return ResponseEntity.ok().body(experiment);
    }

    /**
     * Clear all old Experiment entries related to the study and create new ones. Return updated experiment list.
     * @param studyId ID of the study.
     * @param experiments List of experiment to be used in experiment entries
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createExperiments(@RequestParam Long studyId, @RequestBody List<Experiment> experiments,
                                               @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.STUDY_OWNER);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<Experiment> newExperiments = this.experimentService.createExperimentEntries(studyId, experiments);
            return ResponseEntity.status(HttpStatus.CREATED).body(newExperiments);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
