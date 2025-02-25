package io.passport.server.service;

import io.passport.server.model.LearningProcessParameter;
import io.passport.server.model.LearningProcessParameterId;
import io.passport.server.repository.LearningProcessParameterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for LearningProcessParameter management.
 */
@Service
public class LearningProcessParameterService {

    /**
     * LearningProcessParameter repo access for database management.
     */
    private final LearningProcessParameterRepository learningProcessParameterRepository;

    @Autowired
    public LearningProcessParameterService(LearningProcessParameterRepository learningProcessParameterRepository) {
        this.learningProcessParameterRepository = learningProcessParameterRepository;
    }

    /**
     * Return all LearningProcessParameters
     * @return
     */
    public List<LearningProcessParameter> getAllLearningProcessParameters() {
        return learningProcessParameterRepository.findAll();
    }

    /**
     * Find LearningProcessParameters by learningProcessId
     * @param learningProcessId ID of the LearningProcess
     * @return
     */
    public List<LearningProcessParameter> findByLearningProcessId(Long learningProcessId) {
        return learningProcessParameterRepository.findByIdLearningProcessId(learningProcessId);
    }

    /**
     * Find LearningProcessParameters by parameterId
     * @param parameterId ID of the Parameter
     * @return
     */
    public List<LearningProcessParameter> findByParameterId(Long parameterId) {
        return learningProcessParameterRepository.findByIdParameterId(parameterId);
    }

    /**
     * Find a LearningProcessParameter by composite id
     * @param learningProcessParameterId composite ID of the LearningProcessParameter
     * @return
     */
    public Optional<LearningProcessParameter> findLearningProcessParameterById(LearningProcessParameterId learningProcessParameterId) {
        return learningProcessParameterRepository.findById(learningProcessParameterId);
    }

    /**
     * Save a LearningProcessParameter
     * @param learningProcessParameter LearningProcessParameter to be saved
     * @return
     */
    public LearningProcessParameter saveLearningProcessParameter(LearningProcessParameter learningProcessParameter) {
        return learningProcessParameterRepository.save(learningProcessParameter);
    }

    /**
     * Update a LearningProcessParameter
     * @param learningProcessParameterId composite ID of the LearningProcessParameter
     * @param updatedLearningProcessParameter LearningProcessParameter to be updated
     * @return
     */
    public Optional<LearningProcessParameter> updateLearningProcessParameter(LearningProcessParameterId learningProcessParameterId, LearningProcessParameter updatedLearningProcessParameter) {
        Optional<LearningProcessParameter> oldLearningProcessParameter = learningProcessParameterRepository.findById(learningProcessParameterId);
        if (oldLearningProcessParameter.isPresent()) {
            LearningProcessParameter learningProcessParameter = oldLearningProcessParameter.get();
            learningProcessParameter.setType(updatedLearningProcessParameter.getType());
            learningProcessParameter.setValue(updatedLearningProcessParameter.getValue());
            LearningProcessParameter savedLearningProcessParameter = learningProcessParameterRepository.save(learningProcessParameter);
            return Optional.of(savedLearningProcessParameter);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a LearningProcessParameter
     * @param learningProcessParameterId composite ID of LearningProcessParameter to be deleted
     * @return
     */
    public Optional<LearningProcessParameter> deleteLearningProcessParameter(LearningProcessParameterId learningProcessParameterId) {
        Optional<LearningProcessParameter> existingProcessParameter = learningProcessParameterRepository.findById(learningProcessParameterId);
        if (existingProcessParameter.isPresent()) {
            learningProcessParameterRepository.delete(existingProcessParameter.get());
            return existingProcessParameter;
        } else {
            return Optional.empty();
        }
    }

}
