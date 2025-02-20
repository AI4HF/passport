package io.passport.server.controller;

import io.passport.server.model.*;
import io.passport.server.service.LearningDatasetService;
import io.passport.server.service.RoleCheckerService;
import org.keycloak.KeycloakPrincipal;
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
    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER, Role.DATA_SCIENTIST);

    @Autowired
    public LearningDatasetController(LearningDatasetService learningDatasetService, RoleCheckerService roleCheckerService) {
        this.learningDatasetService = learningDatasetService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read a LearningDataset by id
     * @param studyId ID of the study for authorization
     * @param learningDatasetId ID of the LearningDataset
     * @param principal KeycloakPrincipal object that holds access token
     * @return ResponseEntity with LearningDataset
     */
    @GetMapping("/{learningDatasetId}")
    public ResponseEntity<?> getLearningDataset(@RequestParam Long studyId,
                                                @PathVariable Long learningDatasetId,
                                                @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<LearningDataset> learningDataset = this.learningDatasetService.findLearningDatasetByLearningDatasetId(learningDatasetId);
        return learningDataset.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Read all LearningDatasets or filtered by dataTransformationId and/or datasetId
     * @param studyId ID of the study for authorization
     * @param dataTransformationId ID of the DataTransformation (optional)
     * @param datasetId ID of the Dataset (optional)
     * @param principal KeycloakPrincipal object that holds access token
     * @return ResponseEntity with list of LearningDatasets
     */
    @GetMapping()
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
     * Create LearningDataset with corresponding Dataset Transformation.
     * @param studyId ID of the study for authorization
     * @param request LearningDataset and Transformation model instance to be created
     * @param principal KeycloakPrincipal object that holds access token
     * @return ResponseEntity with created LearningDatasetAndTransformationDTO
     */
    @PostMapping()
    public ResponseEntity<?> createLearningDatasetWithTransformation(@RequestParam Long studyId,
                                                                     @RequestBody LearningDatasetandTransformationDTO request,
                                                                     @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            LearningDatasetandTransformationDTO response = learningDatasetService.createLearningDatasetAndTransformation(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update both DatasetTransformation and LearningDataset in a single transaction.
     * @param studyId ID of the study for authorization
     * @param learningDatasetId ID of the LearningDataset to be updated
     * @param request LearningDatasetAndTransformationRequest containing updated DatasetTransformation and LearningDataset
     * @param principal KeycloakPrincipal object that holds access token
     * @return ResponseEntity with updated LearningDataset and DatasetTransformation
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

            Optional<LearningDatasetandTransformationDTO> updatedEntities = learningDatasetService.updateLearningDatasetWithTransformation(transformation, learningDataset);

            if (updatedEntities.isPresent()) {
                return ResponseEntity.ok().body(updatedEntities.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("LearningDataset or DatasetTransformation not found");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by LearningDataset ID.
     * @param studyId ID of the study for authorization
     * @param learningDatasetId ID of the LearningDataset that is to be deleted
     * @param principal KeycloakPrincipal object that holds access token
     * @return ResponseEntity
     */
    @DeleteMapping("/{learningDatasetId}")
    public ResponseEntity<?> deleteLearningDataset(@RequestParam Long studyId,
                                                   @PathVariable Long learningDatasetId,
                                                   @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.learningDatasetService.deleteLearningDataset(learningDatasetId);
            return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
