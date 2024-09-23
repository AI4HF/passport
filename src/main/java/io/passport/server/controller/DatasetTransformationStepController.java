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

    /**
     * DatasetTransformationStep service for DatasetTransformationStep management
     */
    private final DatasetTransformationStepService datasetTransformationStepService;

    /**
     * Role checker service for authorization
     */
    private final RoleCheckerService roleCheckerService;

    /**
     * List of authorized roles for this endpoint
     */
    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);

    @Autowired
    public DatasetTransformationStepController(DatasetTransformationStepService datasetTransformationStepService, RoleCheckerService roleCheckerService) {
        this.datasetTransformationStepService = datasetTransformationStepService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read all DatasetTransformationSteps or filtered by dataTransformationId
     * @param dataTransformationId ID of the DatasetTransformation (optional)
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<DatasetTransformationStep>> getDatasetTransformationSteps(
            @RequestParam(required = false) Long dataTransformationId,
            @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<DatasetTransformationStep> steps;

        if (dataTransformationId != null) {
            steps = this.datasetTransformationStepService.findByDataTransformationId(dataTransformationId);
        } else {
            steps = this.datasetTransformationStepService.getAllDatasetTransformationSteps();
        }

        long totalCount = steps.size();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(steps);
    }


    /**
     * Read a DatasetTransformationStep by id
     * @param stepId ID of the DatasetTransformationStep
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping("/{stepId}")
    public ResponseEntity<?> getDatasetTransformationStep(@PathVariable Long stepId,
                                                          @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<DatasetTransformationStep> datasetTransformationStep = this.datasetTransformationStepService.findDatasetTransformationStepByStepId(stepId);

        if(datasetTransformationStep.isPresent()) {
            return ResponseEntity.ok().body(datasetTransformationStep.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create DatasetTransformationStep.
     * @param datasetTransformationStep DatasetTransformationStep model instance to be created.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createDatasetTransformationStep(@RequestBody DatasetTransformationStep datasetTransformationStep,
                                                             @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            DatasetTransformationStep savedDatasetTransformationStep = this.datasetTransformationStepService.saveDatasetTransformationStep(datasetTransformationStep);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDatasetTransformationStep);
        } catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update DatasetTransformationStep.
     * @param stepId ID of the DatasetTransformationStep that is to be updated.
     * @param updatedDatasetTransformationStep DatasetTransformationStep model instance with updated details.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PutMapping("/{stepId}")
    public ResponseEntity<?> updateDatasetTransformationStep(@PathVariable Long stepId,
                                                             @RequestBody DatasetTransformationStep updatedDatasetTransformationStep,
                                                             @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<DatasetTransformationStep> savedDatasetTransformationStep = this.datasetTransformationStepService.updateDatasetTransformationStep(stepId, updatedDatasetTransformationStep);
            if(savedDatasetTransformationStep.isPresent()) {
                return ResponseEntity.ok().body(savedDatasetTransformationStep);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by DatasetTransformationStep ID.
     * @param stepId ID of the DatasetTransformationStep that is to be deleted.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @DeleteMapping("/{stepId}")
    public ResponseEntity<?> deleteDatasetTransformationStep(@PathVariable Long stepId,
                                                             @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.datasetTransformationStepService.deleteDatasetTransformationStep(stepId);
            if(isDeleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
