package io.passport.server.controller;

import io.passport.server.model.*;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.PopulationService;
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
 * Class which stores the generated HTTP requests related to population operations.
 */
@RestController
@RequestMapping("/population")
public class PopulationController {

    private static final Logger log = LoggerFactory.getLogger(PopulationController.class);

    private final String relationName = "Population";
    private final PopulationService populationService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    private final List<Role> allowedRoles = List.of(Role.STUDY_OWNER, Role.DATA_ENGINEER);
    private final List<Role> viewOnlyRoles = List.of(Role.STUDY_OWNER, Role.DATA_SCIENTIST, Role.DATA_ENGINEER, Role.SURVEY_MANAGER, Role.QUALITY_ASSURANCE_SPECIALIST, Role.ML_ENGINEER);
    @Autowired
    public PopulationController(PopulationService populationService,
                                RoleCheckerService roleCheckerService,
                                AuditLogBookService auditLogBookService) {
        this.populationService = populationService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Read population by populationId.
     *
     * @param populationId ID of the population.
     * @param studyId      ID of the study.
     * @param principal    Jwt principal containing user info.
     * @return ResponseEntity with the population data.
     */
    @GetMapping("/{populationId}")
    public ResponseEntity<?> getPopulationById(@PathVariable("populationId") String populationId,
                                               @RequestParam String studyId,
                                               @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, viewOnlyRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Population> population = this.populationService.findPopulationById(populationId);
        return population.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Read population by studyId.
     *
     * @param studyId   ID of the study.
     * @param principal Jwt principal containing user info.
     * @return ResponseEntity with the list of populations.
     */
    @GetMapping
    public ResponseEntity<?> getPopulationByStudyId(@RequestParam String studyId,
                                                    @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, viewOnlyRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Population> populations = this.populationService.findPopulationByStudyId(studyId);
        return ResponseEntity.ok().body(populations);
    }

    /**
     * Create Population.
     * (Only STUDY_OWNER is allowed to create)
     *
     * @param population Population model instance to be created.
     * @param studyId    ID of the study.
     * @param principal  Jwt principal containing user info.
     * @return ResponseEntity with the created population data.
     */
    @PostMapping
    public ResponseEntity<?> createPopulation(@RequestBody Population population,
                                              @RequestParam String studyId,
                                              @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Population savedPopulation = this.populationService.savePopulation(population);

            if (savedPopulation.getPopulationId() != null) {
                String recordId = savedPopulation.getPopulationId();
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.CREATE,
                        relationName,
                        recordId,
                        savedPopulation
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPopulation);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Population.
     * (Only STUDY_OWNER is allowed to update)
     *
     * @param populationId      ID of the population to be updated.
     * @param updatedPopulation Updated population model.
     * @param studyId           ID of the study.
     * @param principal         Jwt principal containing user info.
     * @return ResponseEntity with the updated population data.
     */
    @PutMapping("/{populationId}")
    public ResponseEntity<?> updatePopulation(@PathVariable String populationId,
                                              @RequestBody Population updatedPopulation,
                                              @RequestParam String studyId,
                                              @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Optional<Population> savedPopulationOpt =
                    this.populationService.updatePopulation(populationId, updatedPopulation);

            if (savedPopulationOpt.isPresent()) {
                Population savedPopulation = savedPopulationOpt.get();
                String recordId = savedPopulation.getPopulationId();
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.UPDATE,
                        relationName,
                        recordId,
                        savedPopulation
                );
                return ResponseEntity.ok(savedPopulation);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete Population by populationId.
     * (Only STUDY_OWNER is allowed to delete)
     *
     * @param populationId ID of the population to be deleted.
     * @param studyId      ID of the study.
     * @param principal    Jwt principal containing user info.
     * @return ResponseEntity with no content if successful.
     */
    @DeleteMapping("/{populationId}")
    public ResponseEntity<?> deletePopulation(@PathVariable String populationId,
                                              @RequestParam String studyId,
                                              @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Optional<Population> deletedPopulation = this.populationService.deletePopulation(populationId);
            if (deletedPopulation.isPresent()) {
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.DELETE,
                        relationName,
                        populationId,
                        deletedPopulation.get()
                );
                return ResponseEntity.status(HttpStatus.OK).body(deletedPopulation.get());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
