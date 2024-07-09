package io.passport.server.controller;

import io.passport.server.model.DatasetTransformation;
import io.passport.server.service.DatasetTransformationService;
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
 * Class which stores the generated HTTP requests related to DatasetTransformation operations.
 */
@RestController
@RequestMapping("/dataset-transformation")
public class DatasetTransformationController {
    private static final Logger log = LoggerFactory.getLogger(DatasetTransformationController.class);

    /**
     * DatasetTransformation service for DatasetTransformation management
     */
    private final DatasetTransformationService datasetTransformationService;

    @Autowired
    public DatasetTransformationController(DatasetTransformationService datasetTransformationService) {
        this.datasetTransformationService = datasetTransformationService;
    }

    /**
     * Read all DatasetTransformations
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<DatasetTransformation>> getAllDatasetTransformations() {
        List<DatasetTransformation> datasetTransformations = this.datasetTransformationService.getAllDatasetTransformations();

        long totalCount = datasetTransformations.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(datasetTransformations);
    }

    /**
     * Read a DatasetTransformation by id
     * @param dataTransformationId ID of the DatasetTransformation
     * @return
     */
    @GetMapping("/{dataTransformationId}")
    public ResponseEntity<?> getDatasetTransformation(@PathVariable Long dataTransformationId) {
        Optional<DatasetTransformation> datasetTransformation = this.datasetTransformationService.findDatasetTransformationByDataTransformationId(dataTransformationId);

        if(datasetTransformation.isPresent()) {
            return ResponseEntity.ok().body(datasetTransformation.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create DatasetTransformation.
     * @param datasetTransformation DatasetTransformation model instance to be created.
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createDatasetTransformation(@RequestBody DatasetTransformation datasetTransformation) {
        try{
            DatasetTransformation savedDatasetTransformation = this.datasetTransformationService.saveDatasetTransformation(datasetTransformation);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDatasetTransformation);
        } catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update DatasetTransformation.
     * @param dataTransformationId ID of the DatasetTransformation that is to be updated.
     * @param updatedDatasetTransformation DatasetTransformation model instance with updated details.
     * @return
     */
    @PutMapping("/{dataTransformationId}")
    public ResponseEntity<?> updateDatasetTransformation(@PathVariable Long dataTransformationId, @RequestBody DatasetTransformation updatedDatasetTransformation) {
        try{
            Optional<DatasetTransformation> savedDatasetTransformation = this.datasetTransformationService.updateDatasetTransformation(dataTransformationId, updatedDatasetTransformation);
            if(savedDatasetTransformation.isPresent()) {
                return ResponseEntity.ok().body(savedDatasetTransformation);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by DatasetTransformation ID.
     * @param dataTransformationId ID of the DatasetTransformation that is to be deleted.
     * @return
     */
    @DeleteMapping("/{dataTransformationId}")
    public ResponseEntity<?> deleteDatasetTransformation(@PathVariable Long dataTransformationId) {
        try{
            boolean isDeleted = this.datasetTransformationService.deleteDatasetTransformation(dataTransformationId);
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
