package io.passport.server.controller;

import io.passport.server.model.DatasetTransformation;
import io.passport.server.model.LearningDataset;
import io.passport.server.model.LearningDatasetandTransformationDTO;
import io.passport.server.service.LearningDatasetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    /**
     * LearningDataset service for LearningDataset management
     */
    private final LearningDatasetService learningDatasetService;

    @Autowired
    public LearningDatasetController(LearningDatasetService learningDatasetService) {
        this.learningDatasetService = learningDatasetService;
    }

    /**
     * Read a LearningDataset by id
     * @param learningDatasetId ID of the LearningDataset
     * @return
     */
    @GetMapping("/{learningDatasetId}")
    public ResponseEntity<?> getLearningDataset(@PathVariable Long learningDatasetId) {
        Optional<LearningDataset> learningDataset = this.learningDatasetService.findLearningDatasetByLearningDatasetId(learningDatasetId);

        if(learningDataset.isPresent()) {
            return ResponseEntity.ok().body(learningDataset.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Read all LearningDatasets or filtered by dataTransformationId and/or datasetId
     * @param dataTransformationId ID of the DataTransformation (optional)
     * @param datasetId ID of the Dataset (optional)
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<LearningDataset>> getLearningDatasets(
            @RequestParam(required = false) Long dataTransformationId,
            @RequestParam(required = false) Long datasetId) {

        List<LearningDataset> datasets;

        if (dataTransformationId != null && datasetId != null) {
            return ResponseEntity.badRequest().build();
        } else if (dataTransformationId != null) {
            datasets = this.learningDatasetService.findByDataTransformationId(dataTransformationId);
        } else if (datasetId != null) {
            datasets = this.learningDatasetService.findByDatasetId(datasetId);
        } else {
            datasets = this.learningDatasetService.getAllLearningDatasets();
        }

        return ResponseEntity.ok().body(datasets);
    }


    /**
     * Create LearningDataset with corresponding Dataset Transformation.
     * @param request LearningDataset and Transformation model instance to be created.
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createLearningDatasetWithTransformation(@RequestBody LearningDatasetandTransformationDTO request) {
        try {
            LearningDatasetandTransformationDTO response = learningDatasetService.createLearningDatasetAndTransformation(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update both DatasetTransformation and LearningDataset in a single transaction.
     * @param learningDatasetId ID of the LearningDataset to be updated.
     * @param request LearningDatasetAndTransformationRequest containing updated DatasetTransformation and LearningDataset.
     * @return ResponseEntity with updated LearningDataset and DatasetTransformation
     */
    @PutMapping("/{learningDatasetId}")
    public ResponseEntity<?> updateLearningDatasetWithTransformation(
            @PathVariable Long learningDatasetId,
            @RequestBody LearningDatasetandTransformationDTO request
    ) {
        try {
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
     * @param learningDatasetId ID of the LearningDataset that is to be deleted.
     * @return
     */
    @DeleteMapping("/{learningDatasetId}")
    public ResponseEntity<?> deleteLearningDataset(@PathVariable Long learningDatasetId) {
        try{
            boolean isDeleted = this.learningDatasetService.deleteLearningDataset(learningDatasetId);
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
