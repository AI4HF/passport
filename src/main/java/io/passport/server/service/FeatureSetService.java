package io.passport.server.service;

import io.passport.server.model.FeatureSet;
import io.passport.server.repository.FeatureSetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
     * Return all FeatureSets for an assigned personnel
     * @param personnelId ID of the personnel
     * @return
     */
    public List<FeatureSet> getAllFeatureSets(String personnelId) {
        return featureSetRepository.findFeatureSetByPersonnelId(personnelId);
    }

    /**
     * Find a FeatureSet by featureSetId for an assigned personnel
     * @param featureSetId ID of the FeatureSet
     * @param personnelId ID of the personnel
     * @return
     */
    public Optional<FeatureSet> findFeatureSetByFeatureSetId(Long featureSetId, String personnelId) {
        return featureSetRepository.findByIdAndPersonnelId(featureSetId, personnelId);
    }

    /**
     * Save a FeatureSet
     * @param featureSet FeatureSet to be saved
     * @return
     */
    public FeatureSet saveFeatureSet(FeatureSet featureSet) {
        featureSet.setCreatedAt(Instant.now());
        featureSet.setLastUpdatedAt(Instant.now());
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
            featureSet.setCreatedBy(updatedFeatureSet.getCreatedBy());
            featureSet.setLastUpdatedAt(Instant.now());
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
