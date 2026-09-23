package io.passport.server.controller;

import io.passport.server.model.*;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.ModelEvaluationDatasetService;
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
import java.util.stream.Collectors;

/**
 * Class which stores the generated HTTP requests related to ModelEvaluationDataset operations.
 */
@RestController
@RequestMapping("/model-evaluation-dataset")
public class ModelEvaluationDatasetController {

    private static final Logger log = LoggerFactory.getLogger(ModelEvaluationDatasetController.class);

    private final String relationName = "Model Evaluation Dataset";
    private final ModelEvaluationDatasetService modelEvaluationDatasetService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    private final List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);

    @Autowired
    public ModelEvaluationDatasetController(ModelEvaluationDatasetService modelEvaluationDatasetService,
                                            RoleCheckerService roleCheckerService,
                                            AuditLogBookService auditLogBookService) {
        this.modelEvaluationDatasetService = modelEvaluationDatasetService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Read ModelEvaluationDatasets filtered by modelEvaluationId and/or learningDatasetId.
     * @param studyId ID of the study for authorization
     * @param modelEvaluationId ID of the ModelEvaluation (optional)
     * @param learningDatasetId ID of the LearningDataset (optional)
     * @param principal Jwt principal containing user info
     * @return List of ModelEvaluationDatasetDTOs
     */
    @GetMapping
    public ResponseEntity<List<ModelEvaluationDatasetDTO>> getModelEvaluationDatasets(
            @RequestParam String studyId,
            @RequestParam(required = false) String modelEvaluationId,
            @RequestParam(required = false) String learningDatasetId,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedToViewStudy(studyId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ModelEvaluationDataset> links;
        if (modelEvaluationId != null && learningDatasetId != null) {
            ModelEvaluationDatasetId id = new ModelEvaluationDatasetId(modelEvaluationId, learningDatasetId);
            Optional<ModelEvaluationDataset> link = this.modelEvaluationDatasetService.findModelEvaluationDatasetById(id);
            if (link.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            links = List.of(link.get());
        } else if (modelEvaluationId != null) {
            links = this.modelEvaluationDatasetService.findByModelEvaluationId(modelEvaluationId);
        } else if (learningDatasetId != null) {
            links = this.modelEvaluationDatasetService.findByLearningDatasetId(learningDatasetId);
        } else {
            return ResponseEntity.badRequest().build();
        }

        List<ModelEvaluationDatasetDTO> dtos = links.stream()
                .map(ModelEvaluationDatasetDTO::new)
                .collect(Collectors.toList());

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(dtos.size()));
        return ResponseEntity.ok().headers(headers).body(dtos);
    }

    /**
     * Create a new ModelEvaluationDataset entity.
     * @param studyId ID of the study for authorization
     * @param modelEvaluationDatasetDTO DTO containing data for the new ModelEvaluationDataset
     * @param principal Jwt principal containing user info
     * @return Created ModelEvaluationDataset
     */
    @PostMapping
    public ResponseEntity<?> createModelEvaluationDataset(@RequestParam String studyId,
                                                          @RequestBody ModelEvaluationDatasetDTO modelEvaluationDatasetDTO,
                                                          @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            ModelEvaluationDataset entity = new ModelEvaluationDataset(modelEvaluationDatasetDTO);
            ModelEvaluationDataset saved = this.modelEvaluationDatasetService.saveModelEvaluationDataset(entity);

            if (saved.getId() != null) {
                String compositeId = "(" + saved.getId().getModelEvaluationId() + ", " + saved.getId().getLearningDatasetId() + ")";
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.CREATE,
                        relationName,
                        compositeId,
                        saved
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {
            log.error("Error creating ModelEvaluationDataset: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update a ModelEvaluationDataset using query parameters.
     * @param studyId ID of the study for authorization
     * @param modelEvaluationId ID of the ModelEvaluation
     * @param learningDatasetId ID of the LearningDataset
     * @param updatedModelEvaluationDataset Updated details
     * @param principal Jwt principal containing user info
     * @return Updated ModelEvaluationDataset or NOT_FOUND
     */
    @PutMapping
    public ResponseEntity<?> updateModelEvaluationDataset(
            @RequestParam String studyId,
            @RequestParam String modelEvaluationId,
            @RequestParam String learningDatasetId,
            @RequestBody ModelEvaluationDataset updatedModelEvaluationDataset,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ModelEvaluationDatasetId id = new ModelEvaluationDatasetId(modelEvaluationId, learningDatasetId);

        try {
            Optional<ModelEvaluationDataset> savedOpt =
                    this.modelEvaluationDatasetService.updateModelEvaluationDataset(id, updatedModelEvaluationDataset);
            if (savedOpt.isPresent()) {
                String compositeId = "(" + modelEvaluationId + ", " + learningDatasetId + ")";
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.UPDATE,
                        relationName,
                        compositeId,
                        savedOpt.get()
                );
                return ResponseEntity.ok(savedOpt.get());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error updating ModelEvaluationDataset: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete a ModelEvaluationDataset by composite ID using query parameters.
     * @param studyId ID of the study for authorization
     * @param modelEvaluationId ID of the ModelEvaluation
     * @param learningDatasetId ID of the LearningDataset
     * @param principal Jwt principal containing user info
     * @return Deleted ModelEvaluationDataset or NOT_FOUND
     */
    @DeleteMapping
    public ResponseEntity<?> deleteModelEvaluationDataset(
            @RequestParam String studyId,
            @RequestParam String modelEvaluationId,
            @RequestParam String learningDatasetId,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ModelEvaluationDatasetId id = new ModelEvaluationDatasetId(modelEvaluationId, learningDatasetId);

        try {
            Optional<ModelEvaluationDataset> deletedLink = this.modelEvaluationDatasetService.deleteModelEvaluationDataset(id);
            if (deletedLink.isPresent()) {
                String compositeId = "(" + modelEvaluationId + ", " + learningDatasetId + ")";
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.DELETE,
                        relationName,
                        compositeId,
                        deletedLink.get()
                );
                return ResponseEntity.status(HttpStatus.OK).body(deletedLink.get());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error deleting ModelEvaluationDataset: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
