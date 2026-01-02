package io.passport.server.service;

import io.passport.server.model.EvaluationMeasure;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.EvaluationMeasureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for EvaluationMeasure management.
 */
@Service
public class EvaluationMeasureService {

    private final EvaluationMeasureRepository evaluationMeasureRepository;
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public EvaluationMeasureService(EvaluationMeasureRepository evaluationMeasureRepository,
                                    RoleCheckerService roleCheckerService) {
        this.evaluationMeasureRepository = evaluationMeasureRepository;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Determines which entities are to be cascaded based on the request from the previous element in the chain
     * Continues the chain by directing to the next entries through the other validation method
     *
     * @param studyId Id of the Study
     * @param sourceResourceType Resource type of the parent element in the Cascade chain
     * @param sourceResourceId Resource id of the parent element in the Cascade chain
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateCascade(String studyId, String sourceResourceType, String sourceResourceId, Jwt principal) {
        List<EvaluationMeasure> affectedMeasures;

        switch (sourceResourceType) {
            case "Model":
                affectedMeasures = evaluationMeasureRepository.findAllByModelId(sourceResourceId);
                break;
            default:
                return new ValidationResult(true, "");
        }

        if (affectedMeasures.isEmpty()) {
            return new ValidationResult(true, "");
        }

        boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                studyId,
                principal,
                List.of(Role.DATA_SCIENTIST)
        );

        if (!hasPermission) {
            return new ValidationResult(false, "EvaluationMeasure");
        }

        return new ValidationResult(true, "EvaluationMeasure");
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
