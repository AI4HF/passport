package io.passport.server.service;

import io.passport.server.model.FeatureSet;
import io.passport.server.repository.FeatureSetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for FeatureSet management.
 */
@Service
public class FeatureSetService {

    /**
     * FeatureSet repo access for database management.
     */
    private final FeatureSetRepository featureSetRepository;

    @Autowired
    public FeatureSetService(FeatureSetRepository featureSetRepository) {
        this.featureSetRepository = featureSetRepository;
    }

    /**
     * Return all FeatureSets
     * @return
     */
    public List<FeatureSet> getAllFeatureSets() {
        return featureSetRepository.findAll();
    }

    /**
     * Find a FeatureSet by featureSetId
     * @param featureSetId ID of the FeatureSet
     * @return
     */
    public Optional<FeatureSet> findFeatureSetByFeatureSetId(Long featureSetId) {
        return featureSetRepository.findById(featureSetId);
    }

    /**
     * Save a FeatureSet
     * @param featureSet FeatureSet to be saved
     * @return
     */
    public FeatureSet saveFeatureSet(FeatureSet featureSet) {
        return featureSetRepository.save(featureSet);
    }

    /**
     * Update a FeatureSet
     * @param featureSetId ID of the FeatureSet
     * @param updatedFeatureSet FeatureSet to be updated
     * @return
     */
    public Optional<FeatureSet> updateFeatureSet(Long featureSetId, FeatureSet updatedFeatureSet) {
        Optional<FeatureSet> oldFeatureSet = featureSetRepository.findById(featureSetId);
        if (oldFeatureSet.isPresent()) {
            FeatureSet featureSet = oldFeatureSet.get();
            featureSet.setTitle(updatedFeatureSet.getTitle());
            featureSet.setFeaturesetURL(updatedFeatureSet.getFeaturesetURL());
            featureSet.setDescription(updatedFeatureSet.getDescription());
            featureSet.setCreatedAt(updatedFeatureSet.getCreatedAt());
            featureSet.setCreatedBy(updatedFeatureSet.getCreatedBy());
            featureSet.setLastUpdatedAt(updatedFeatureSet.getLastUpdatedAt());
            featureSet.setLastUpdatedBy(updatedFeatureSet.getLastUpdatedBy());
            FeatureSet savedFeatureSet = featureSetRepository.save(featureSet);
            return Optional.of(savedFeatureSet);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a FeatureSet
     * @param featureSetId ID of FeatureSet to be deleted
     * @return
     */
    public boolean deleteFeatureSet(Long featureSetId) {
        if(featureSetRepository.existsById(featureSetId)) {
            featureSetRepository.deleteById(featureSetId);
            return true;
        } else {
            return false;
        }
    }
}
