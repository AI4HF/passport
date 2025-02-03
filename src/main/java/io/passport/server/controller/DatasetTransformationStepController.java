package io.passport.server.controller;

import io.passport.server.model.DatasetTransformationStep;
import io.passport.server.model.Role;
import io.passport.server.service.AuditLogBookService; // <-- NEW
import io.passport.server.service.DatasetTransformationStepService;
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
 * Class which stores the generated HTTP requests related to DatasetTransformationStep operations.
 */
@RestController
@RequestMapping("/dataset-transformation-step")
public class DatasetTransformationStepController {

    private static final Logger log = LoggerFactory.getLogger(DatasetTransformationStepController.class);

    private final DatasetTransformationStepService datasetTransformationStepService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService; // <-- NEW

    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);

    @Autowired
    public DatasetTransformationStepController(DatasetTransformationStepService datasetTransformationStepService,
                                               RoleCheckerService roleCheckerService,
                                               AuditLogBookService auditLogBookService) {
        this.datasetTransformationStepService = datasetTransformationStepService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Retrieves all DatasetTransformationSteps or filters by dataTransformationId if provided.
     *
     * @param dataTransformationId  Optional ID to filter steps
     * @param studyId               ID of the study for authorization
     * @param principal             Jwt principal containing user info
     * @return                      List of DatasetTransformationSteps
     */
    @GetMapping
    public ResponseEntity<List<DatasetTransformationStep>> getDatasetTransformationSteps(
            @RequestParam(required = false) Long dataTransformationId,
            @RequestParam Long studyId,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<DatasetTransformationStep> steps = (dataTransformationId != null)
                ? this.datasetTransformationStepService.findByDataTransformationId(dataTransformationId)
                : this.datasetTransformationStepService.getAllDatasetTransformationSteps();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(steps.size()));
        return ResponseEntity.ok().headers(headers).body(steps);
    }

    /**
     * Retrieves a single DatasetTransformationStep by stepId.
     *
     * @param stepId     ID of the step to retrieve
     * @param studyId    ID of the study for authorization
     * @param principal  Jwt principal containing user info
     * @return           The requested step or NOT_FOUND
     */
    @GetMapping("/{stepId}")
    public ResponseEntity<?> getDatasetTransformationStep(@PathVariable Long stepId,
                                                          @RequestParam Long studyId,
                                                          @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<DatasetTransformationStep> stepOpt = this.datasetTransformationStepService.findDatasetTransformationStepByStepId(stepId);
        return stepOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new DatasetTransformationStep.
     *
     * @param datasetTransformationStep  The step data to create
     * @param studyId                    ID of the study for authorization
     * @param principal                  Jwt principal containing user info
     * @return                           Created step or BAD_REQUEST on error
     */
    @PostMapping
    public ResponseEntity<?> createDatasetTransformationStep(@RequestBody DatasetTransformationStep datasetTransformationStep,
                                                             @RequestParam Long studyId,
                                                             @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            DatasetTransformationStep saved = this.datasetTransformationStepService
                    .saveDatasetTransformationStep(datasetTransformationStep);

            if (saved.getStepId() != null) {
                String recordId = saved.getStepId().toString();
                String description = "Creation of DatasetTransformationStep " + recordId;
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        studyId,
                        "CREATE",
                        "DatasetTransformationStep",
                        recordId,
                        saved,
                        description
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {
            log.error("Error creating DatasetTransformationStep: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Updates an existing DatasetTransformationStep by stepId.
     *
     * @param stepId                     ID of the step to update
     * @param updatedDatasetTransformationStep  Updated details
     * @param studyId                    ID of the study for authorization
     * @param principal                  Jwt principal containing user info
     * @return                           Updated step or NOT_FOUND
     */
    @PutMapping("/{stepId}")
    public ResponseEntity<?> updateDatasetTransformationStep(@PathVariable Long stepId,
                                                             @RequestBody DatasetTransformationStep updatedDatasetTransformationStep,
                                                             @RequestParam Long studyId,
                                                             @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<DatasetTransformationStep> savedOpt =
                    this.datasetTransformationStepService.updateDatasetTransformationStep(stepId, updatedDatasetTransformationStep);

            if (savedOpt.isPresent()) {
                DatasetTransformationStep saved = savedOpt.get();
                if (saved.getStepId() != null) {
                    String recordId = saved.getStepId().toString();
                    String description = "Update of DatasetTransformationStep " + recordId;
                    auditLogBookService.createAuditLog(
                            principal.getSubject(),
                            studyId,
                            "UPDATE",
                            "DatasetTransformationStep",
                            recordId,
                            saved,
                            description
                    );
                }
                return ResponseEntity.ok(saved);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating DatasetTransformationStep: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Deletes a DatasetTransformationStep by stepId.
     *
     * @param stepId    ID of the step to delete
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return          NO_CONTENT if deleted, NOT_FOUND otherwise
     */
    @DeleteMapping("/{stepId}")
    public ResponseEntity<?> deleteDatasetTransformationStep(@PathVariable Long stepId,
                                                             @RequestParam Long studyId,
                                                             @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.datasetTransformationStepService.deleteDatasetTransformationStep(stepId);
            if (isDeleted) {
                String description = "Deletion of DatasetTransformationStep " + stepId;
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        studyId,
                        "DELETE",
                        "DatasetTransformationStep",
                        stepId.toString(),
                        null,
                        description
                );
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting DatasetTransformationStep: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
