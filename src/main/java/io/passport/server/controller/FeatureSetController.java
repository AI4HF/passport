package io.passport.server.controller;

import io.passport.server.model.FeatureSet;
import io.passport.server.service.FeatureSetService;
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
 * Class which stores the generated HTTP requests related to FeatureSet operations.
 */
@RestController
@RequestMapping("/featureset")
public class FeatureSetController {
    private static final Logger log = LoggerFactory.getLogger(FeatureSetController.class);

    /**
     * FeatureSet service for FeatureSet management
     */
    private final FeatureSetService featureSetService;

    @Autowired
    public FeatureSetController(FeatureSetService featureSetService) {
        this.featureSetService = featureSetService;
    }

    /**
     * Read all FeatureSets
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<FeatureSet>> getAllFeatureSets() {
        List<FeatureSet> featureSets = this.featureSetService.getAllFeatureSets();

        long totalCount = featureSets.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(featureSets);
    }

    /**
     * Read a FeatureSet by id
     * @param featureSetId ID of the FeatureSet
     * @return
     */
    @GetMapping("/{featureSetId}")
    public ResponseEntity<?> getFeatureSet(@PathVariable Long featureSetId) {
        Optional<FeatureSet> featureSet = this.featureSetService.findFeatureSetByFeatureSetId(featureSetId);

        if(featureSet.isPresent()) {
            return ResponseEntity.ok().body(featureSet.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create FeatureSet.
     * @param featureSet FeatureSet model instance to be created.
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createFeatureSet(@RequestBody FeatureSet featureSet) {
        try{
            FeatureSet savedFeatureSet = this.featureSetService.saveFeatureSet(featureSet);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedFeatureSet);
        } catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update FeatureSet.
     * @param featureSetId ID of the FeatureSet that is to be updated.
     * @param updatedFeatureSet FeatureSet model instance with updated details.
     * @return
     */
    @PutMapping("/{featureSetId}")
    public ResponseEntity<?> updateFeatureSet(@PathVariable Long featureSetId, @RequestBody FeatureSet updatedFeatureSet) {
        try{
            Optional<FeatureSet> savedFeatureSet = this.featureSetService.updateFeatureSet(featureSetId, updatedFeatureSet);
            if(savedFeatureSet.isPresent()) {
                return ResponseEntity.ok().body(savedFeatureSet);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by FeatureSet ID.
     * @param featureSetId ID of the FeatureSet that is to be deleted.
     * @return
     */
    @DeleteMapping("/{featureSetId}")
    public ResponseEntity<?> deleteFeatureSet(@PathVariable Long featureSetId) {
        try{
            boolean isDeleted = this.featureSetService.deleteFeatureSet(featureSetId);
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

