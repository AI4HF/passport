package io.passport.server.controller;

import io.passport.server.model.DatasetTransformationStep;
import io.passport.server.model.Role;
import io.passport.server.service.DatasetTransformationStepService;
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
 * Class which stores the generated HTTP requests related to DatasetTransformationStep operations.
 */
@RestController
@RequestMapping("/dataset-transformation-step")
public class DatasetTransformationStepController {
    private static final Logger log = LoggerFactory.getLogger(DatasetTransformationStepController.class);

    private final DatasetTransformationStepService datasetTransformationStepService;
    private final RoleCheckerService roleCheckerService;
    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);

    @Autowired
    public DatasetTransformationStepController(DatasetTransformationStepService datasetTransformationStepService, RoleCheckerService roleCheckerService) {
        this.datasetTransformationStepService = datasetTransformationStepService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read all DatasetTransformationSteps or filtered by dataTransformationId
     * @param dataTransformationId ID of the DatasetTransformation (optional)
     * @param studyId ID of the study
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<DatasetTransformationStep>> getDatasetTransformationSteps(
            @RequestParam(required = false) Long dataTransformationId,
            @RequestParam Long studyId,
            @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<DatasetTransformationStep> steps = (dataTransformationId != null) ?
                this.datasetTransformationStepService.findByDataTransformationId(dataTransformationId) :
                this.datasetTransformationStepService.getAllDatasetTransformationSteps();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(steps.size()));

        return ResponseEntity.ok().headers(headers).body(steps);
    }

    /**
     * Read a DatasetTransformationStep by id
     * @param stepId ID of the DatasetTransformationStep
     * @param studyId ID of the study
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping("/{stepId}")
    public ResponseEntity<?> getDatasetTransformationStep(@PathVariable Long stepId,
                                                          @RequestParam Long studyId,
                                                          @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<DatasetTransformationStep> datasetTransformationStep = this.datasetTransformationStepService.findDatasetTransformationStepByStepId(stepId);
        return datasetTransformationStep.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create DatasetTransformationStep.
     * @param datasetTransformationStep DatasetTransformationStep model instance to be created.
     * @param studyId ID of the study
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createDatasetTransformationStep(@RequestBody DatasetTransformationStep datasetTransformationStep,
                                                             @RequestParam Long studyId,
                                                             @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            DatasetTransformationStep savedDatasetTransformationStep = this.datasetTransformationStepService.saveDatasetTransformationStep(datasetTransformationStep);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDatasetTransformationStep);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update DatasetTransformationStep.
     * @param stepId ID of the DatasetTransformationStep that is to be updated.
     * @param updatedDatasetTransformationStep DatasetTransformationStep model instance with updated details.
     * @param studyId ID of the study
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PutMapping("/{stepId}")
    public ResponseEntity<?> updateDatasetTransformationStep(@PathVariable Long stepId,
                                                             @RequestBody DatasetTransformationStep updatedDatasetTransformationStep,
                                                             @RequestParam Long studyId,
                                                             @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<DatasetTransformationStep> savedDatasetTransformationStep = this.datasetTransformationStepService.updateDatasetTransformationStep(stepId, updatedDatasetTransformationStep);
            return savedDatasetTransformationStep.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by DatasetTransformationStep ID.
     * @param stepId ID of the DatasetTransformationStep that is to be deleted.
     * @param studyId ID of the study
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @DeleteMapping("/{stepId}")
    public ResponseEntity<?> deleteDatasetTransformationStep(@PathVariable Long stepId,
                                                             @RequestParam Long studyId,
                                                             @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.datasetTransformationStepService.deleteDatasetTransformationStep(stepId);
            return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

