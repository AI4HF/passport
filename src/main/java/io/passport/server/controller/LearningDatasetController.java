package io.passport.server.controller;

import io.passport.server.model.LearningDataset;
import io.passport.server.service.LearningDatasetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
     * Read all LearningDatasets
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<LearningDataset>> getAllLearningDatasets() {
        List<LearningDataset> learningDatasets = this.learningDatasetService.getAllLearningDatasets();

        long totalCount = learningDatasets.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(learningDatasets);
    }

    /**
     * Read LearningDatasets by dataTransformationId
     * @param dataTransformationId ID of the DataTransformation
     * @return
     */
    @GetMapping("/transformation/{dataTransformationId}")
    public ResponseEntity<List<LearningDataset>> getLearningDatasetsByTransformationId(@PathVariable Long dataTransformationId) {
        List<LearningDataset> datasets = this.learningDatasetService.findByDataTransformationId(dataTransformationId);
        if (datasets.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(datasets);
    }

    /**
     * Read LearningDatasets by datasetId
     * @param datasetId ID of the Dataset
     * @return
     */
    @GetMapping("/dataset/{datasetId}")
    public ResponseEntity<List<LearningDataset>> getLearningDatasetsByDatasetId(@PathVariable Long datasetId) {
        List<LearningDataset> datasets = this.learningDatasetService.findByDatasetId(datasetId);
        if (datasets.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(datasets);
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
     * Create LearningDataset.
     * @param learningDataset LearningDataset model instance to be created.
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createLearningDataset(@RequestBody LearningDataset learningDataset) {
        try{
            LearningDataset savedLearningDataset = this.learningDatasetService.saveLearningDataset(learningDataset);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedLearningDataset);
        } catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update LearningDataset.
     * @param learningDatasetId ID of the LearningDataset that is to be updated.
     * @param updatedLearningDataset LearningDataset model instance with updated details.
     * @return
     */
    @PutMapping("/{learningDatasetId}")
    public ResponseEntity<?> updateLearningDataset(@PathVariable Long learningDatasetId, @RequestBody LearningDataset updatedLearningDataset) {
        try{
            Optional<LearningDataset> savedLearningDataset = this.learningDatasetService.updateLearningDataset(learningDatasetId, updatedLearningDataset);
            if(savedLearningDataset.isPresent()) {
                return ResponseEntity.ok().body(savedLearningDataset);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e){
            log.error(e.getMessage());
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
