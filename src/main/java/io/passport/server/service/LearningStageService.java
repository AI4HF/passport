package io.passport.server.service;

import io.passport.server.model.LearningStage;
import io.passport.server.repository.LearningStageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for learning stage management.
 */
@Service
public class LearningStageService {

    /**
     * LearningStage repo access for database management.
     */
    private final LearningStageRepository learningStageRepository;

    @Autowired
    public LearningStageService(LearningStageRepository learningStageRepository) {
        this.learningStageRepository = learningStageRepository;
    }

    /**
     * Return all learning stages
     * @return
     */
    public List<LearningStage> getAllLearningStages() {
        return learningStageRepository.findAll();
    }

    /**
     * Return all learning stages by Learning Process ID
     * @param learningProcessId ID of the learning process
     * @return
     */
    public List<LearningStage> findLearningStagesByProcessId(Long learningProcessId) {
        return learningStageRepository.findByLearningProcessId(learningProcessId);
    }

    /**
     * Find a learning stage by learningStageId
     * @param learningStageId ID of the learning stage
     * @return
     */
    public Optional<LearningStage> findLearningStageById(Long learningStageId) {
        return learningStageRepository.findById(learningStageId);
    }

    /**
     * Save a learning stage
     * @param learningStage learning stage to be saved
     * @return
     */
    public LearningStage saveLearningStage(LearningStage learningStage) {
        return learningStageRepository.save(learningStage);
    }

    /**
     * Update a learning stage
     * @param learningStageId ID of the learning stage
     * @param updatedLearningStage learning stage to be updated
     * @return
     */
    public Optional<LearningStage> updateLearningStage(Long learningStageId, LearningStage updatedLearningStage) {
        Optional<LearningStage> oldLearningStage = learningStageRepository.findById(learningStageId);
        if (oldLearningStage.isPresent()) {
            LearningStage learningStage = oldLearningStage.get();
            learningStage.setLearningProcessId(updatedLearningStage.getLearningProcessId());
            learningStage.setLearningStageName(updatedLearningStage.getLearningStageName());
            learningStage.setDescription(updatedLearningStage.getDescription());
            learningStage.setDatasetPercentage(updatedLearningStage.getDatasetPercentage());
            LearningStage savedLearningStage = learningStageRepository.save(learningStage);
            return Optional.of(savedLearningStage);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a learning stage
     * @param learningStageId ID of learning stage to be deleted
     * @return
     */
    public boolean deleteLearningStage(Long learningStageId) {
        if(learningStageRepository.existsById(learningStageId)) {
            learningStageRepository.deleteById(learningStageId);
            return true;
        }else{
            return false;
        }
    }
}
