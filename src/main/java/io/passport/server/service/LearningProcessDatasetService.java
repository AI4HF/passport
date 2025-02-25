package io.passport.server.service;

import io.passport.server.model.LearningProcessDataset;
import io.passport.server.model.LearningProcessDatasetId;
import io.passport.server.repository.LearningProcessDatasetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for LearningProcessDataset management.
 */
@Service
public class LearningProcessDatasetService {

    /**
     * LearningProcessDataset repo access for database management.
     */
    private final LearningProcessDatasetRepository learningProcessDatasetRepository;

    @Autowired
    public LearningProcessDatasetService(LearningProcessDatasetRepository learningProcessDatasetRepository) {
        this.learningProcessDatasetRepository = learningProcessDatasetRepository;
    }

    /**
     * Return all LearningProcessDatasets
     * @return
     */
    public List<LearningProcessDataset> getAllLearningProcessDatasets() {
        return learningProcessDatasetRepository.findAll();
    }

    /**
     * Find LearningProcessDatasets by learningProcessId
     * @param learningProcessId ID of the LearningProcess
     * @return
     */
    public List<LearningProcessDataset> findByLearningProcessId(Long learningProcessId) {
        return learningProcessDatasetRepository.findByIdLearningProcessId(learningProcessId);
    }

    /**
     * Find LearningProcessDatasets by learningDatasetId
     * @param learningDatasetId ID of the LearningDataset
     * @return
     */
    public List<LearningProcessDataset> findByLearningDatasetId(Long learningDatasetId) {
        return learningProcessDatasetRepository.findByIdLearningDatasetId(learningDatasetId);
    }

    /**
     * Find a LearningProcessDataset by composite id
     * @param learningProcessDatasetId composite ID of the LearningProcessDataset
     * @return
     */
    public Optional<LearningProcessDataset> findLearningProcessDatasetById(LearningProcessDatasetId learningProcessDatasetId) {
        return learningProcessDatasetRepository.findById(learningProcessDatasetId);
    }

    /**
     * Save a LearningProcessDataset
     * @param learningProcessDataset LearningProcessDataset to be saved
     * @return
     */
    public LearningProcessDataset saveLearningProcessDataset(LearningProcessDataset learningProcessDataset) {
        return learningProcessDatasetRepository.save(learningProcessDataset);
    }

    /**
     * Update a LearningProcessDataset
     * @param learningProcessDatasetId composite ID of the LearningProcessDataset
     * @param updatedLearningProcessDataset LearningProcessDataset to be updated
     * @return
     */
    public Optional<LearningProcessDataset> updateLearningProcessDataset(LearningProcessDatasetId learningProcessDatasetId, LearningProcessDataset updatedLearningProcessDataset) {
        Optional<LearningProcessDataset> oldLearningProcessDataset = learningProcessDatasetRepository.findById(learningProcessDatasetId);
        if (oldLearningProcessDataset.isPresent()) {
            LearningProcessDataset learningProcessDataset = oldLearningProcessDataset.get();
            learningProcessDataset.setDescription(updatedLearningProcessDataset.getDescription());
            LearningProcessDataset savedLearningProcessDataset = learningProcessDatasetRepository.save(learningProcessDataset);
            return Optional.of(savedLearningProcessDataset);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a LearningProcessDataset
     * @param learningProcessDatasetId composite ID of LearningProcessDataset to be deleted
     * @return
     */
    public Optional<LearningProcessDataset> deleteLearningProcessDataset(LearningProcessDatasetId learningProcessDatasetId) {
        Optional<LearningProcessDataset> existingProcessDataset = learningProcessDatasetRepository.findById(learningProcessDatasetId);
        if (existingProcessDataset.isPresent()) {
            learningProcessDatasetRepository.delete(existingProcessDataset.get());
            return existingProcessDataset;
        } else {
            return Optional.empty();
        }
    }

}
