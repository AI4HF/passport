package io.passport.server.controller;

import io.passport.server.model.EvaluationMeasure;
import io.passport.server.model.Role;
import io.passport.server.service.EvaluationMeasureService;
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
 * Class which stores the generated HTTP requests related to EvaluationMeasure operations.
 */
@RestController
@RequestMapping("/evaluation-measure")
public class EvaluationMeasureController {
    private static final Logger log = LoggerFactory.getLogger(EvaluationMeasureController.class);
    private final EvaluationMeasureService evaluationMeasureService;
    private final RoleCheckerService roleCheckerService;
    private final List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);

    @Autowired
    public EvaluationMeasureController(EvaluationMeasureService evaluationMeasureService, RoleCheckerService roleCheckerService) {
        this.evaluationMeasureService = evaluationMeasureService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read all EvaluationMeasures by modelId.
     * @param modelId ID of the model
     * @param studyId ID of the study
     * @param principal KeycloakPrincipal object that holds access token
     * @return List of EvaluationMeasures
     */
    @GetMapping()
    public ResponseEntity<List<EvaluationMeasure>> getAllEvaluationMeasuresByModelId(@RequestParam Long modelId,
                                                                                     @RequestParam Long studyId,
                                                                                     @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<EvaluationMeasure> evaluationMeasures = this.evaluationMeasureService.findEvaluationMeasuresByModelId(modelId);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(evaluationMeasures.size()));

        return ResponseEntity.ok().headers(headers).body(evaluationMeasures);
    }

    /**
     * Read an EvaluationMeasure by id.
     * @param evaluationMeasureId ID of the EvaluationMeasure
     * @param studyId ID of the study for authorization
     * @param principal KeycloakPrincipal object that holds access token
     * @return EvaluationMeasure entity or not found
     */
    @GetMapping("/{evaluationMeasureId}")
    public ResponseEntity<?> getEvaluationMeasureById(@PathVariable Long evaluationMeasureId,
                                                      @RequestParam Long studyId,
                                                      @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<EvaluationMeasure> evaluationMeasure = this.evaluationMeasureService.findEvaluationMeasureById(evaluationMeasureId);
        return evaluationMeasure.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create an EvaluationMeasure.
     * @param evaluationMeasure evaluationMeasure model instance to be created
     * @param studyId ID of the study for authorization
     * @param principal KeycloakPrincipal object that holds access token
     * @return Created evaluationMeasure
     */
    @PostMapping()
    public ResponseEntity<?> createEvaluationMeasure(@RequestBody EvaluationMeasure evaluationMeasure,
                                                     @RequestParam Long studyId,
                                                     @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            EvaluationMeasure savedEvaluationMeasure = this.evaluationMeasureService.saveEvaluationMeasure(evaluationMeasure);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedEvaluationMeasure);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update an EvaluationMeasure.
     * @param evaluationMeasureId ID of the EvaluationMeasure that is to be updated
     * @param updatedEvaluationMeasure model instance with updated details
     * @param studyId ID of the study for authorization
     * @param principal KeycloakPrincipal object that holds access token
     * @return Updated EvaluationMeasure
     */
    @PutMapping("/{evaluationMeasureId}")
    public ResponseEntity<?> updateEvaluationMeasure(@PathVariable Long evaluationMeasureId,
                                                     @RequestBody EvaluationMeasure updatedEvaluationMeasure,
                                                     @RequestParam Long studyId,
                                                     @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<EvaluationMeasure> savedEvaluationMeasure = this.evaluationMeasureService.updateEvaluationMeasure(evaluationMeasureId, updatedEvaluationMeasure);
            return savedEvaluationMeasure.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete an EvaluationMeasure by ID.
     * @param evaluationMeasureId ID of the EvaluationMeasure that is to be deleted
     * @param studyId ID of the study for authorization
     * @param principal KeycloakPrincipal object that holds access token
     * @return No content or not found status
     */
    @DeleteMapping("/{evaluationMeasureId}")
    public ResponseEntity<?> deleteEvaluationMeasure(@PathVariable Long evaluationMeasureId,
                                                     @RequestParam Long studyId,
                                                     @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.evaluationMeasureService.deleteEvaluationMeasure(evaluationMeasureId);
            return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
