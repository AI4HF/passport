package io.passport.server.service;

import io.passport.server.model.LearningDataset;
import io.passport.server.repository.LearningDatasetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for LearningDataset management.
 */
@Service
public class LearningDatasetService {

    /**
     * LearningDataset repo access for database management.
     */
    private final LearningDatasetRepository learningDatasetRepository;

    @Autowired
    public LearningDatasetService(LearningDatasetRepository learningDatasetRepository) {
        this.learningDatasetRepository = learningDatasetRepository;
    }

    /**
     * Return all LearningDatasets
     * @return
     */
    public List<LearningDataset> getAllLearningDatasets() {
        return learningDatasetRepository.findAll();
    }

    /**
     * Find LearningDatasets by dataTransformationId
     * @param dataTransformationId ID of the DataTransformation
     * @return
     */
    public List<LearningDataset> findByDataTransformationId(Long dataTransformationId) {
        return learningDatasetRepository.findByDataTransformationId(dataTransformationId);
    }

    /**
     * Find LearningDatasets by datasetId
     * @param datasetId ID of the Dataset
     * @return
     */
    public List<LearningDataset> findByDatasetId(Long datasetId) {
        return learningDatasetRepository.findByDatasetId(datasetId);
    }

    /**
     * Find a LearningDataset by learningDatasetId
     * @param learningDatasetId ID of the LearningDataset
     * @return
     */
    public Optional<LearningDataset> findLearningDatasetByLearningDatasetId(Long learningDatasetId) {
        return learningDatasetRepository.findById(learningDatasetId);
    }

    /**
     * Save a LearningDataset
     * @param learningDataset LearningDataset to be saved
     * @return
     */
    public LearningDataset saveLearningDataset(LearningDataset learningDataset) {
        return learningDatasetRepository.save(learningDataset);
    }

    /**
     * Update a LearningDataset
     * @param learningDatasetId ID of the LearningDataset
     * @param updatedLearningDataset LearningDataset to be updated
     * @return
     */
    public Optional<LearningDataset> updateLearningDataset(Long learningDatasetId, LearningDataset updatedLearningDataset) {
        Optional<LearningDataset> oldLearningDataset = learningDatasetRepository.findById(learningDatasetId);
        if (oldLearningDataset.isPresent()) {
            LearningDataset learningDataset = oldLearningDataset.get();
            learningDataset.setDatasetId(updatedLearningDataset.getDatasetId());
            learningDataset.setDataTransformationId(updatedLearningDataset.getDataTransformationId());
            learningDataset.setDescription(updatedLearningDataset.getDescription());
            LearningDataset savedLearningDataset = learningDatasetRepository.save(learningDataset);
            return Optional.of(savedLearningDataset);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a LearningDataset
     * @param learningDatasetId ID of LearningDataset to be deleted
     * @return
     */
    public boolean deleteLearningDataset(Long learningDatasetId) {
        if(learningDatasetRepository.existsById(learningDatasetId)) {
            learningDatasetRepository.deleteById(learningDatasetId);
            return true;
        } else {
            return false;
        }
    }
}
