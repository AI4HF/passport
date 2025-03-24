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
     * Return all FeatureSets
     * @return
     */
    public List<FeatureSet> getAllFeatureSets() {
        return featureSetRepository.findAll();
    }

    /**
     * Return all FeatureSets by studyId
     * @param studyId ID of the study
     * @return
     */
    public List<FeatureSet> getAllFeatureSetsByStudyId(String studyId) {
        return featureSetRepository.findFeatureSetByStudyId(studyId);
    }

    /**
     * Find a FeatureSet by featureSetId
     * @param featureSetId ID of the FeatureSet
     * @return
     */
    public Optional<FeatureSet> findFeatureSetByFeatureSetId(String featureSetId) {
        return featureSetRepository.findById(featureSetId);
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
    public Optional<FeatureSet> updateFeatureSet(String featureSetId, FeatureSet updatedFeatureSet) {
        Optional<FeatureSet> oldFeatureSet = featureSetRepository.findById(featureSetId);
        if (oldFeatureSet.isPresent()) {
            FeatureSet featureSet = oldFeatureSet.get();
            featureSet.setTitle(updatedFeatureSet.getTitle());
            featureSet.setFeaturesetURL(updatedFeatureSet.getFeaturesetURL());
            featureSet.setDescription(updatedFeatureSet.getDescription());
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
    public Optional<FeatureSet> deleteFeatureSet(String featureSetId) {
        Optional<FeatureSet> existingFeatureSet = featureSetRepository.findById(featureSetId);
        if (existingFeatureSet.isPresent()) {
            featureSetRepository.delete(existingFeatureSet.get());
            return existingFeatureSet;
        } else {
            return Optional.empty();
        }
    }

}
