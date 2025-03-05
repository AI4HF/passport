package io.passport.server.service;

import io.passport.server.model.Feature;
import io.passport.server.repository.FeatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Feature management.
 */
@Service
public class FeatureService {

    /**
     * Feature repo access for database management.
     */
    private final FeatureRepository featureRepository;

    @Autowired
    public FeatureService(FeatureRepository featureRepository) {
        this.featureRepository = featureRepository;
    }

    /**
     * Return all Features
     * @return
     */
    public List<Feature> getAllFeatures() {
        return featureRepository.findAll();
    }

    /**
     * Find Features by featuresetId
     * @param featuresetId ID of the FeatureSet
     * @return
     */
    public List<Feature> findByFeaturesetId(Long featuresetId) {
        return featureRepository.findByFeaturesetId(featuresetId);
    }

    /**
     * Find a Feature by featureId
     * @param featureId ID of the Feature
     * @return
     */
    public Optional<Feature> findFeatureByFeatureId(Long featureId) {
        return featureRepository.findById(featureId);
    }

    /**
     * Save a Feature
     * @param feature Feature to be saved
     * @return
     */
    public Feature saveFeature(Feature feature) {
        feature.setCreatedAt(Instant.now());
        feature.setLastUpdatedAt(Instant.now());
        return featureRepository.save(feature);
    }

    /**
     * Update a Feature
     * @param featureId ID of the Feature
     * @param updatedFeature Feature to be updated
     * @return
     */
    public Optional<Feature> updateFeature(Long featureId, Feature updatedFeature) {
        Optional<Feature> oldFeature = featureRepository.findById(featureId);
        if (oldFeature.isPresent()) {
            Feature feature = oldFeature.get();
            feature.setFeaturesetId(updatedFeature.getFeaturesetId());
            feature.setTitle(updatedFeature.getTitle());
            feature.setDescription(updatedFeature.getDescription());
            feature.setDataType(updatedFeature.getDataType());
            feature.setFeatureType(updatedFeature.getFeatureType());
            feature.setMandatory(updatedFeature.getMandatory());
            feature.setIsUnique(updatedFeature.getIsUnique());
            feature.setUnits(updatedFeature.getUnits());
            feature.setEquipment(updatedFeature.getEquipment());
            feature.setDataCollection(updatedFeature.getDataCollection());
            feature.setLastUpdatedAt(Instant.now());
            feature.setLastUpdatedBy(updatedFeature.getLastUpdatedBy());
            Feature savedFeature = featureRepository.save(feature);
            return Optional.of(savedFeature);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a Feature
     * @param featureId ID of Feature to be deleted
     * @return
     */
    public Optional<Feature> deleteFeature(Long featureId) {
        Optional<Feature> existingFeature = featureRepository.findById(featureId);
        if (existingFeature.isPresent()) {
            featureRepository.delete(existingFeature.get());
            return existingFeature;
        } else {
            return Optional.empty();
        }
    }

}
