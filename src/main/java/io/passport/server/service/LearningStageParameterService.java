package io.passport.server.service;

import io.passport.server.model.LearningStageParameter;
import io.passport.server.model.LearningStageParameterId;
import io.passport.server.repository.LearningStageParameterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for LearningStageParameter management.
 */
@Service
public class LearningStageParameterService {

    /**
     * LearningStageParameter repo access for database management.
     */
    private final LearningStageParameterRepository learningStageParameterRepository;

    @Autowired
    public LearningStageParameterService(LearningStageParameterRepository learningStageParameterRepository) {
        this.learningStageParameterRepository = learningStageParameterRepository;
    }

    /**
     * Return all LearningStageParameters
     * @return
     */
    public List<LearningStageParameter> getAllLearningStageParameters() {
        return learningStageParameterRepository.findAll();
    }

    /**
     * Find LearningStageParameters by learningStageId
     * @param learningStageId ID of the LearningStage
     * @return
     */
    public List<LearningStageParameter> findByLearningStageId(String learningStageId) {
        return learningStageParameterRepository.findByIdLearningStageId(learningStageId);
    }

    /**
     * Find LearningStageParameters by parameterId
     * @param parameterId ID of the Parameter
     * @return
     */
    public List<LearningStageParameter> findByParameterId(String parameterId) {
        return learningStageParameterRepository.findByIdParameterId(parameterId);
    }

    /**
     * Find a LearningStageParameter by composite id
     * @param learningStageParameterId composite ID of the LearningStageParameter
     * @return
     */
    public Optional<LearningStageParameter> findLearningStageParameterById(LearningStageParameterId learningStageParameterId) {
        return learningStageParameterRepository.findById(learningStageParameterId);
    }

    /**
     * Save a LearningStageParameter
     * @param learningStageParameter LearningStageParameter to be saved
     * @return
     */
    public LearningStageParameter saveLearningStageParameter(LearningStageParameter learningStageParameter) {
        return learningStageParameterRepository.save(learningStageParameter);
    }

    /**
     * Update a LearningStageParameter
     * @param learningStageParameterId composite ID of the LearningStageParameter
     * @param updatedLearningStageParameter LearningStageParameter to be updated
     * @return
     */
    public Optional<LearningStageParameter> updateLearningStageParameter(LearningStageParameterId learningStageParameterId, LearningStageParameter updatedLearningStageParameter) {
        Optional<LearningStageParameter> oldLearningStageParameter = learningStageParameterRepository.findById(learningStageParameterId);
        if (oldLearningStageParameter.isPresent()) {
            LearningStageParameter learningStageParameter = oldLearningStageParameter.get();
            learningStageParameter.setType(updatedLearningStageParameter.getType());
            learningStageParameter.setValue(updatedLearningStageParameter.getValue());
            LearningStageParameter savedLearningStageParameter = learningStageParameterRepository.save(learningStageParameter);
            return Optional.of(savedLearningStageParameter);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a LearningStageParameter
     * @param learningStageParameterId composite ID of LearningStageParameter to be deleted
     * @return
     */
    public Optional<LearningStageParameter> deleteLearningStageParameter(LearningStageParameterId learningStageParameterId) {
        Optional<LearningStageParameter> existingParameter = learningStageParameterRepository.findById(learningStageParameterId);
        if (existingParameter.isPresent()) {
            learningStageParameterRepository.delete(existingParameter.get());
            return existingParameter;
        } else {
            return Optional.empty();
        }
    }

}
