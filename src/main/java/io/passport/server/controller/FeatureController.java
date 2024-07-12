package io.passport.server.controller;

import io.passport.server.model.Feature;
import io.passport.server.service.FeatureService;
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
 * Class which stores the generated HTTP requests related to Feature operations.
 */
@RestController
@RequestMapping("/feature")
public class FeatureController {
    private static final Logger log = LoggerFactory.getLogger(FeatureController.class);

    /**
     * Feature service for Feature management
     */
    private final FeatureService featureService;

    @Autowired
    public FeatureController(FeatureService featureService) {
        this.featureService = featureService;
    }

    /**
     * Read all Features
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<Feature>> getAllFeatures() {
        List<Feature> features = this.featureService.getAllFeatures();

        long totalCount = features.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(features);
    }

    /**
     * Read Features by FeatureSet id
     * @param featuresetId ID of the FeatureSet
     * @return
     */
    @GetMapping("/featureset/{featuresetId}")
    public ResponseEntity<List<Feature>> getFeaturesByFeatureSetId(@PathVariable Long featuresetId) {
        List<Feature> features = this.featureService.findByFeaturesetId(featuresetId);
        return ResponseEntity.ok().body(features);
    }

    /**
     * Read a Feature by id
     * @param featureId ID of the Feature
     * @return
     */
    @GetMapping("/{featureId}")
    public ResponseEntity<?> getFeature(@PathVariable Long featureId) {
        Optional<Feature> feature = this.featureService.findFeatureByFeatureId(featureId);

        if(feature.isPresent()) {
            return ResponseEntity.ok().body(feature.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create Feature.
     * @param feature Feature model instance to be created.
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createFeature(@RequestBody Feature feature) {
        try{
            Feature savedFeature = this.featureService.saveFeature(feature);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedFeature);
        } catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Feature.
     * @param featureId ID of the Feature that is to be updated.
     * @param updatedFeature Feature model instance with updated details.
     * @return
     */
    @PutMapping("/{featureId}")
    public ResponseEntity<?> updateFeature(@PathVariable Long featureId, @RequestBody Feature updatedFeature) {
        try{
            Optional<Feature> savedFeature = this.featureService.updateFeature(featureId, updatedFeature);
            if(savedFeature.isPresent()) {
                return ResponseEntity.ok().body(savedFeature);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by Feature ID.
     * @param featureId ID of the Feature that is to be deleted.
     * @return
     */
    @DeleteMapping("/{featureId}")
    public ResponseEntity<?> deleteFeature(@PathVariable Long featureId) {
        try{
            boolean isDeleted = this.featureService.deleteFeature(featureId);
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
