package io.passport.server.service;

import io.passport.server.model.EvaluationMeasure;
import io.passport.server.repository.EvaluationMeasureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for EvaluationMeasure management.
 */
@Service
public class EvaluationMeasureService {

    /**
     * EvaluationMeasure repo access for database management.
     */
    private final EvaluationMeasureRepository evaluationMeasureRepository;

    @Autowired
    public EvaluationMeasureService(EvaluationMeasureRepository evaluationMeasureRepository) {
        this.evaluationMeasureRepository = evaluationMeasureRepository;
    }

    /**
     * Get all EvaluationMeasures
     */
    public List<EvaluationMeasure> getAllParameters() {
        return evaluationMeasureRepository.findAll();
    }

    /**
     * Find EvaluationMeasures by studyId
     * @param modelId The ID of the model
     * @return
     */
    public List<EvaluationMeasure> findEvaluationMeasuresByModelId(String modelId) {
        return evaluationMeasureRepository.findAllByModelId(modelId);
    }

    /**
     * Find an EvaluationMeasure by measureId
     * @param measureId ID of the EvaluationMeasure
     * @return
     */
    public Optional<EvaluationMeasure> findEvaluationMeasureById(String measureId) {
        return evaluationMeasureRepository.findById(measureId);
    }

    /**
     * Save an EvaluationMeasure
     * @param evaluationMeasure EvaluationMeasure to be saved
     * @return
     */
    public EvaluationMeasure saveEvaluationMeasure(EvaluationMeasure evaluationMeasure) {
        return evaluationMeasureRepository.save(evaluationMeasure);
    }

    /**
     * Update an EvaluationMeasure
     * @param measureId ID of the EvaluationMeasure
     * @param updatedEvaluationMeasure EvaluationMeasure to be updated
     * @return
     */
    public Optional<EvaluationMeasure> updateEvaluationMeasure(String measureId, EvaluationMeasure updatedEvaluationMeasure) {
        Optional<EvaluationMeasure> oldEvaluationMeasure = evaluationMeasureRepository.findById(measureId);
        if (oldEvaluationMeasure.isPresent()) {
            EvaluationMeasure evaluationMeasure = oldEvaluationMeasure.get();
            evaluationMeasure.setModelId(updatedEvaluationMeasure.getModelId());
            evaluationMeasure.setName(updatedEvaluationMeasure.getName());
            evaluationMeasure.setDescription(updatedEvaluationMeasure.getDescription());
            evaluationMeasure.setValue(updatedEvaluationMeasure.getValue());
            evaluationMeasure.setDataType(updatedEvaluationMeasure.getDataType());
            EvaluationMeasure savedEvaluationMeasure = evaluationMeasureRepository.save(evaluationMeasure);
            return Optional.of(savedEvaluationMeasure);
        }else{
            return Optional.empty();
        }
    }

    /**
     * Delete an EvaluationMeasure
     * @param measureId ID of EvaluationMeasure to be deleted
     * @return
     */
    public boolean deleteEvaluationMeasure(String measureId) {
        if(evaluationMeasureRepository.existsById(measureId)){
            evaluationMeasureRepository.deleteById(measureId);
            return true;
        }else{
            return false;
        }
    }
}
