package io.passport.server.controller;

import io.passport.server.model.Population;
import io.passport.server.model.Role;
import io.passport.server.service.PopulationService;
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
 * Class which stores the generated HTTP requests related to population operations.
 */
@RestController
@RequestMapping("/population")
public class PopulationController {

    private static final Logger log = LoggerFactory.getLogger(PopulationController.class);

    private final PopulationService populationService;
    private final RoleCheckerService roleCheckerService;
    private final List<Role> allowedRoles = List.of(Role.STUDY_OWNER, Role.DATA_ENGINEER);

    @Autowired
    public PopulationController(PopulationService populationService, RoleCheckerService roleCheckerService) {
        this.populationService = populationService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read population by populationId.
     * @param populationId ID of the population.
     * @param studyId ID of the study.
     * @param principal KeycloakPrincipal object that holds access token.
     * @return ResponseEntity with the population data.
     */
    @GetMapping("/{populationId}")
    public ResponseEntity<?> getPopulationById(@PathVariable("populationId") Long populationId,
                                               @RequestParam Long studyId,
                                               @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        // Check user authorization for the given study
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Population> population = this.populationService.findPopulationById(populationId);
        return population.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Read population by studyId.
     * @param studyId ID of the study.
     * @param principal KeycloakPrincipal object that holds access token.
     * @return ResponseEntity with the list of populations.
     */
    @GetMapping()
    public ResponseEntity<?> getPopulationByStudyId(@RequestParam Long studyId,
                                                    @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        // Check user authorization for the given study
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Population> populations = this.populationService.findPopulationByStudyId(studyId);
        return ResponseEntity.ok().body(populations);
    }

    /**
     * Create Population.
     * @param population Population model instance to be created.
     * @param studyId ID of the study.
     * @param principal KeycloakPrincipal object that holds access token.
     * @return ResponseEntity with the created population data.
     */
    @PostMapping()
    public ResponseEntity<?> createPopulation(@RequestBody Population population,
                                              @RequestParam Long studyId,
                                              @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        // Allowed roles for this endpoint
        List<Role> lesserAllowedRoles = List.of(Role.STUDY_OWNER);
        // Check user authorization for the given study
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, lesserAllowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Population savedPopulation = this.populationService.savePopulation(population);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPopulation);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Population.
     * @param populationId ID of the population to be updated.
     * @param updatedPopulation Updated population model.
     * @param studyId ID of the study.
     * @param principal KeycloakPrincipal object that holds access token.
     * @return ResponseEntity with the updated population data.
     */
    @PutMapping("/{populationId}")
    public ResponseEntity<?> updatePopulation(@PathVariable Long populationId,
                                              @RequestBody Population updatedPopulation,
                                              @RequestParam Long studyId,
                                              @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        // Allowed roles for this endpoint
        List<Role> lesserAllowedRoles = List.of(Role.STUDY_OWNER);
        // Check user authorization for the given study
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, lesserAllowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Optional<Population> savedPopulation = this.populationService.updatePopulation(populationId, updatedPopulation);
            return savedPopulation.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete Population by populationId.
     * @param populationId ID of the population to be deleted.
     * @param studyId ID of the study.
     * @param principal KeycloakPrincipal object that holds access token.
     * @return ResponseEntity with no content if successful.
     */
    @DeleteMapping("/{populationId}")
    public ResponseEntity<?> deletePopulation(@PathVariable Long populationId,
                                              @RequestParam Long studyId,
                                              @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        // Allowed roles for this endpoint
        List<Role> lesserAllowedRoles = List.of(Role.STUDY_OWNER);
        // Check user authorization for the given study
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, lesserAllowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            boolean isDeleted = this.populationService.deletePopulation(populationId);
            if (isDeleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
