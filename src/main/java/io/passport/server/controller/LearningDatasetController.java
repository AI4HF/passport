package io.passport.server.controller;

import io.passport.server.model.*;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.LearningDatasetService;
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
 * Class which stores the generated HTTP requests related to LearningDataset operations.
 */
@RestController
@RequestMapping("/learning-dataset")
public class LearningDatasetController {

    private static final Logger log = LoggerFactory.getLogger(LearningDatasetController.class);

    private final LearningDatasetService learningDatasetService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService; // <-- NEW

    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER, Role.DATA_SCIENTIST);

    @Autowired
    public LearningDatasetController(LearningDatasetService learningDatasetService,
                                     RoleCheckerService roleCheckerService,
                                     AuditLogBookService auditLogBookService) {
        this.learningDatasetService = learningDatasetService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Reads a LearningDataset by its ID.
     *
     * @param studyId           ID of the study for authorization
     * @param learningDatasetId ID of the LearningDataset
     * @param principal         Jwt principal containing user info
     * @return The requested LearningDataset or NOT_FOUND
     */
    @GetMapping("/{learningDatasetId}")
    public ResponseEntity<?> getLearningDataset(@RequestParam Long studyId,
                                                @PathVariable Long learningDatasetId,
                                                @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<LearningDataset> ldOpt = this.learningDatasetService.findLearningDatasetByLearningDatasetId(learningDatasetId);
        return ldOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Reads all LearningDatasets, or filters by dataTransformationId and/or datasetId if provided.
     *
     * @param studyId              ID of the study for authorization
     * @param dataTransformationId Optional DataTransformation ID
     * @param datasetId            Optional Dataset ID
     * @param principal            Jwt principal containing user info
     * @return List of LearningDatasets
     */
    @GetMapping
    public ResponseEntity<List<LearningDataset>> getLearningDatasets(
            @RequestParam Long studyId,
            @RequestParam(required = false) Long dataTransformationId,
            @RequestParam(required = false) Long datasetId,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<LearningDataset> datasets;
        if (dataTransformationId != null) {
            datasets = this.learningDatasetService.findByDataTransformationId(dataTransformationId);
        } else if (datasetId != null) {
            datasets = this.learningDatasetService.findByDatasetId(datasetId);
        } else {
            datasets = this.learningDatasetService.getAllLearningDatasetsByStudyId(studyId);
        }
        return ResponseEntity.ok().body(datasets);
    }

    /**
     * Creates a new LearningDataset along with a DatasetTransformation.
     *
     * @param studyId   ID of the study for authorization
     * @param request   DTO containing both LearningDataset and Transformation info
     * @param principal Jwt principal containing user info
     * @return Created LearningDatasetandTransformationDTO
     */
    @PostMapping
    public ResponseEntity<?> createLearningDatasetWithTransformation(@RequestParam Long studyId,
                                                                     @RequestBody LearningDatasetandTransformationDTO request,
                                                                     @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            LearningDatasetandTransformationDTO createdDTO =
                    learningDatasetService.createLearningDatasetAndTransformation(request);

            LearningDataset newLd = createdDTO.getLearningDataset();
            if (newLd != null && newLd.getLearningDatasetId() != null) {
                String recordId = newLd.getLearningDatasetId().toString();
                String description = "Creation of LearningDataset " + recordId;
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim("preferred_username"),
                        studyId,
                        "CREATE",
                        "LearningDataset",
                        recordId,
                        newLd,
                        description
                );
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(createdDTO);

        } catch (Exception e) {
            log.error("Error creating LearningDataset with transformation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Updates a LearningDataset and its DatasetTransformation in a single transaction.
     *
     * @param studyId           ID of the study for authorization
     * @param learningDatasetId ID of the LearningDataset to update
     * @param request           DTO containing updated LearningDataset and Transformation
     * @param principal         Jwt principal containing user info
     * @return Updated LearningDatasetandTransformationDTO or NOT_FOUND
     */
    @PutMapping("/{learningDatasetId}")
    public ResponseEntity<?> updateLearningDatasetWithTransformation(
            @RequestParam Long studyId,
            @PathVariable Long learningDatasetId,
            @RequestBody LearningDatasetandTransformationDTO request,
            @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            DatasetTransformation transformation = request.getDatasetTransformation();
            LearningDataset learningDataset = request.getLearningDataset();
            learningDataset.setLearningDatasetId(learningDatasetId);

            Optional<LearningDatasetandTransformationDTO> updatedOpt =
                    learningDatasetService.updateLearningDatasetWithTransformation(transformation, learningDataset);

            if (updatedOpt.isPresent()) {
                LearningDatasetandTransformationDTO updatedDTO = updatedOpt.get();
                LearningDataset updatedLd = updatedDTO.getLearningDataset();
                String recordId = updatedLd.getLearningDatasetId().toString();
                String description = "Update of LearningDataset " + recordId;
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim("preferred_username"),
                        studyId,
                        "UPDATE",
                        "LearningDataset",
                        recordId,
                        updatedLd,
                        description
                );
                return ResponseEntity.ok(updatedDTO);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("LearningDataset or DatasetTransformation not found");
            }
        } catch (Exception e) {
            log.error("Error updating LearningDataset with transformation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Deletes a LearningDataset by its ID.
     *
     * @param studyId           ID of the study for authorization
     * @param learningDatasetId ID of the LearningDataset to delete
     * @param principal         Jwt principal containing user info
     * @return NO_CONTENT if deleted, NOT_FOUND otherwise
     */
    @DeleteMapping("/{learningDatasetId}")
    public ResponseEntity<?> deleteLearningDataset(@RequestParam Long studyId,
                                                   @PathVariable Long learningDatasetId,
                                                   @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<LearningDataset> deletedLearningDataset = this.learningDatasetService.deleteLearningDataset(learningDatasetId);
            if (deletedLearningDataset.isPresent()) {
                String description = "Deletion of LearningDataset " + learningDatasetId;
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim("preferred_username"),
                        studyId,
                        "DELETE",
                        "LearningDataset",
                        learningDatasetId.toString(),
                        deletedLearningDataset.get(),
                        description
                );
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(deletedLearningDataset.get());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error deleting LearningDataset: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
