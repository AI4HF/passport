package io.passport.server.controller;

import io.passport.server.model.ModelEvaluation;
import io.passport.server.model.Operation;
import io.passport.server.model.Role;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.ModelEvaluationService;
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
 * Class which stores the generated HTTP requests related to ModelEvaluation operations.
 */
@RestController
@RequestMapping("/model-evaluation")
public class ModelEvaluationController {

    private static final Logger log = LoggerFactory.getLogger(ModelEvaluationController.class);

    private final String relationName = "Model Evaluation";
    private final ModelEvaluationService modelEvaluationService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    private final List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);

    @Autowired
    public ModelEvaluationController(ModelEvaluationService modelEvaluationService,
                                     RoleCheckerService roleCheckerService,
                                     AuditLogBookService auditLogBookService) {
        this.modelEvaluationService = modelEvaluationService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Read all ModelEvaluations of a Model.
     * @param modelId ID of the Model
     * @param studyId ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return List of ModelEvaluations
     */
    @GetMapping()
    public ResponseEntity<List<ModelEvaluation>> getAllModelEvaluationsByModelId(@RequestParam String modelId,
                                                                                @RequestParam String studyId,
                                                                                @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedToViewStudy(studyId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ModelEvaluation> modelEvaluations = this.modelEvaluationService.findModelEvaluationsByModelId(modelId);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(modelEvaluations.size()));

        return ResponseEntity.ok().headers(headers).body(modelEvaluations);
    }

    /**
     * Read a ModelEvaluation by id.
     * @param modelEvaluationId ID of the ModelEvaluation
     * @param studyId ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return ModelEvaluation entity or not found
     */
    @GetMapping("/{modelEvaluationId}")
    public ResponseEntity<?> getModelEvaluationById(@PathVariable String modelEvaluationId,
                                                    @RequestParam String studyId,
                                                    @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedToViewStudy(studyId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<ModelEvaluation> modelEvaluation = this.modelEvaluationService.findModelEvaluationById(modelEvaluationId);
        return modelEvaluation.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create a ModelEvaluation.
     * @param modelEvaluation ModelEvaluation model instance to be created
     * @param studyId ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Created ModelEvaluation
     */
    @PostMapping()
    public ResponseEntity<?> createModelEvaluation(@RequestBody ModelEvaluation modelEvaluation,
                                                   @RequestParam String studyId,
                                                   @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            ModelEvaluation savedModelEvaluation = this.modelEvaluationService.saveModelEvaluation(modelEvaluation);

            if (savedModelEvaluation.getModelEvaluationId() != null) {
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.CREATE,
                        relationName,
                        savedModelEvaluation.getModelEvaluationId(),
                        savedModelEvaluation
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(savedModelEvaluation);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update a ModelEvaluation.
     * @param modelEvaluationId ID of the ModelEvaluation that is to be updated
     * @param updatedModelEvaluation model instance with updated details
     * @param studyId ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Updated ModelEvaluation
     */
    @PutMapping("/{modelEvaluationId}")
    public ResponseEntity<?> updateModelEvaluation(@PathVariable String modelEvaluationId,
                                                   @RequestBody ModelEvaluation updatedModelEvaluation,
                                                   @RequestParam String studyId,
                                                   @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<ModelEvaluation> savedModelEvaluationOpt =
                    this.modelEvaluationService.updateModelEvaluation(modelEvaluationId, updatedModelEvaluation);

            if (savedModelEvaluationOpt.isPresent()) {
                ModelEvaluation savedModelEvaluation = savedModelEvaluationOpt.get();
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.UPDATE,
                        relationName,
                        modelEvaluationId,
                        savedModelEvaluation
                );
                return ResponseEntity.ok(savedModelEvaluation);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete a ModelEvaluation by ID.
     * @param modelEvaluationId ID of the ModelEvaluation that is to be deleted
     * @param studyId ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Deleted ModelEvaluation or not found status
     */
    @DeleteMapping("/{modelEvaluationId}")
    public ResponseEntity<?> deleteModelEvaluation(@PathVariable String modelEvaluationId,
                                                   @RequestParam String studyId,
                                                   @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<ModelEvaluation> deletedModelEvaluation = this.modelEvaluationService.deleteModelEvaluation(modelEvaluationId);
            if (deletedModelEvaluation.isPresent()) {
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.DELETE,
                        relationName,
                        modelEvaluationId,
                        deletedModelEvaluation.get()
                );
                return ResponseEntity.status(HttpStatus.OK).body(deletedModelEvaluation.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Validate the deletion of a ModelEvaluation and everything that hangs off it.
     * @param modelEvaluationId ID of the ModelEvaluation
     * @param studyId ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Validation result of the cascade
     */
    @GetMapping("/{modelEvaluationId}/validate-deletion")
    public ResponseEntity<?> validateModelEvaluationDeletion(@PathVariable String modelEvaluationId,
                                                             @RequestParam String studyId,
                                                             @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(this.modelEvaluationService.validateModelEvaluationDeletion(studyId, modelEvaluationId, principal));
    }
}
