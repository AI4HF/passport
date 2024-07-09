package io.passport.server.controller;

import io.passport.server.model.FeatureDatasetCharacteristic;
import io.passport.server.model.FeatureDatasetCharacteristicId;
import io.passport.server.service.FeatureDatasetCharacteristicService;
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
 * Class which stores the generated HTTP requests related to FeatureDatasetCharacteristic operations.
 */
@RestController
@RequestMapping("/feature-dataset-characteristic")
public class FeatureDatasetCharacteristicController {
    private static final Logger log = LoggerFactory.getLogger(FeatureDatasetCharacteristicController.class);

    /**
     * FeatureDatasetCharacteristic service for FeatureDatasetCharacteristic management
     */
    private final FeatureDatasetCharacteristicService featureDatasetCharacteristicService;

    @Autowired
    public FeatureDatasetCharacteristicController(FeatureDatasetCharacteristicService featureDatasetCharacteristicService) {
        this.featureDatasetCharacteristicService = featureDatasetCharacteristicService;
    }

    /**
     * Read all FeatureDatasetCharacteristics
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<FeatureDatasetCharacteristic>> getAllFeatureDatasetCharacteristics() {
        List<FeatureDatasetCharacteristic> featureDatasetCharacteristics = this.featureDatasetCharacteristicService.getAllFeatureDatasetCharacteristics();

        long totalCount = featureDatasetCharacteristics.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(featureDatasetCharacteristics);
    }

    /**
     * Read FeatureDatasetCharacteristics by datasetId
     * @param datasetId ID of the Dataset
     * @return
     */
    @GetMapping("/dataset/{datasetId}")
    public ResponseEntity<List<FeatureDatasetCharacteristic>> getFeatureDatasetCharacteristicsByDatasetId(@PathVariable Long datasetId) {
        List<FeatureDatasetCharacteristic> characteristics = this.featureDatasetCharacteristicService.findByDatasetId(datasetId);
        if (characteristics.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(characteristics);
    }

    /**
     * Read FeatureDatasetCharacteristics by featureId
     * @param featureId ID of the Feature
     * @return
     */
    @GetMapping("/feature/{featureId}")
    public ResponseEntity<List<FeatureDatasetCharacteristic>> getFeatureDatasetCharacteristicsByFeatureId(@PathVariable Long featureId) {
        List<FeatureDatasetCharacteristic> characteristics = this.featureDatasetCharacteristicService.findByFeatureId(featureId);
        if (characteristics.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(characteristics);
    }

    /**
     * Read a FeatureDatasetCharacteristic by composite id
     * @param datasetId ID of the Dataset
     * @param featureId ID of the Feature
     * @return
     */
    @GetMapping("/dataset/{datasetId}/feature/{featureId}")
    public ResponseEntity<?> getFeatureDatasetCharacteristic(@PathVariable Long datasetId, @PathVariable Long featureId) {
        FeatureDatasetCharacteristicId featureDatasetCharacteristicId = new FeatureDatasetCharacteristicId();
        featureDatasetCharacteristicId.setDatasetId(datasetId);
        featureDatasetCharacteristicId.setFeatureId(featureId);
        Optional<FeatureDatasetCharacteristic> featureDatasetCharacteristic = this.featureDatasetCharacteristicService.findFeatureDatasetCharacteristicById(featureDatasetCharacteristicId);

        if(featureDatasetCharacteristic.isPresent()) {
            return ResponseEntity.ok().body(featureDatasetCharacteristic.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create FeatureDatasetCharacteristic.
     * @param featureDatasetCharacteristic FeatureDatasetCharacteristic model instance to be created.
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createFeatureDatasetCharacteristic(@RequestBody FeatureDatasetCharacteristic featureDatasetCharacteristic) {
        try{
            FeatureDatasetCharacteristic savedFeatureDatasetCharacteristic = this.featureDatasetCharacteristicService.saveFeatureDatasetCharacteristic(featureDatasetCharacteristic);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedFeatureDatasetCharacteristic);
        } catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update FeatureDatasetCharacteristic.
     * @param datasetId ID of the Dataset
     * @param featureId ID of the Feature
     * @param updatedFeatureDatasetCharacteristic FeatureDatasetCharacteristic model instance with updated details.
     * @return
     */
    @PutMapping("/dataset/{datasetId}/feature/{featureId}")
    public ResponseEntity<?> updateFeatureDatasetCharacteristic(@PathVariable Long datasetId, @PathVariable Long featureId, @RequestBody FeatureDatasetCharacteristic updatedFeatureDatasetCharacteristic) {
        FeatureDatasetCharacteristicId featureDatasetCharacteristicId = new FeatureDatasetCharacteristicId();
        featureDatasetCharacteristicId.setDatasetId(datasetId);
        featureDatasetCharacteristicId.setFeatureId(featureId);
        try{
            Optional<FeatureDatasetCharacteristic> savedFeatureDatasetCharacteristic = this.featureDatasetCharacteristicService.updateFeatureDatasetCharacteristic(featureDatasetCharacteristicId, updatedFeatureDatasetCharacteristic);
            if(savedFeatureDatasetCharacteristic.isPresent()) {
                return ResponseEntity.ok().body(savedFeatureDatasetCharacteristic);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by FeatureDatasetCharacteristic composite ID.
     * @param datasetId ID of the Dataset
     * @param featureId ID of the Feature
     * @return
     */
    @DeleteMapping("/dataset/{datasetId}/feature/{featureId}")
    public ResponseEntity<?> deleteFeatureDatasetCharacteristic(@PathVariable Long datasetId, @PathVariable Long featureId) {
        FeatureDatasetCharacteristicId featureDatasetCharacteristicId = new FeatureDatasetCharacteristicId();
        featureDatasetCharacteristicId.setDatasetId(datasetId);
        featureDatasetCharacteristicId.setFeatureId(featureId);
        try{
            boolean isDeleted = this.featureDatasetCharacteristicService.deleteFeatureDatasetCharacteristic(featureDatasetCharacteristicId);
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
