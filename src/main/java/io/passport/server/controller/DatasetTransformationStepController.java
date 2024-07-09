package io.passport.server.controller;

import io.passport.server.model.DatasetTransformationStep;
import io.passport.server.service.DatasetTransformationStepService;
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

    @Autowired
    public DatasetTransformationStepController(DatasetTransformationStepService datasetTransformationStepService) {
        this.datasetTransformationStepService = datasetTransformationStepService;
    }

    /**
     * Read all DatasetTransformationSteps
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<DatasetTransformationStep>> getAllDatasetTransformationSteps() {
        List<DatasetTransformationStep> datasetTransformationSteps = this.datasetTransformationStepService.getAllDatasetTransformationSteps();

        long totalCount = datasetTransformationSteps.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(datasetTransformationSteps);
    }

    /**
     * Read DatasetTransformationSteps by dataTransformationId
     * @param dataTransformationId ID of the DatasetTransformation
     * @return
     */
    @GetMapping("/transformation/{dataTransformationId}")
    public ResponseEntity<List<DatasetTransformationStep>> getDatasetTransformationStepsByTransformationId(@PathVariable Long dataTransformationId) {
        List<DatasetTransformationStep> steps = this.datasetTransformationStepService.findByDataTransformationId(dataTransformationId);
        if (steps.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(steps);
    }

    /**
     * Read a DatasetTransformationStep by id
     * @param stepId ID of the DatasetTransformationStep
     * @return
     */
    @GetMapping("/{stepId}")
    public ResponseEntity<?> getDatasetTransformationStep(@PathVariable Long stepId) {
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
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createDatasetTransformationStep(@RequestBody DatasetTransformationStep datasetTransformationStep) {
        try{
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
     * @return
     */
    @PutMapping("/{stepId}")
    public ResponseEntity<?> updateDatasetTransformationStep(@PathVariable Long stepId, @RequestBody DatasetTransformationStep updatedDatasetTransformationStep) {
        try{
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
     * @return
     */
    @DeleteMapping("/{stepId}")
    public ResponseEntity<?> deleteDatasetTransformationStep(@PathVariable Long stepId) {
        try{
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
