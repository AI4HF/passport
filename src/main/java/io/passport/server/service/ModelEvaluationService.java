package io.passport.server.service;

import io.passport.server.model.ModelEvaluation;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.ModelEvaluationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for ModelEvaluation management.
 */
@Service
public class ModelEvaluationService {

    /**
     * ModelEvaluation repo access for database management.
     */
    private final ModelEvaluationRepository modelEvaluationRepository;
    private final RoleCheckerService roleCheckerService;

    /**
     * Lazy service references for limited use in cascade validation
     */
    @Autowired @Lazy private EvaluationMeasureService evaluationMeasureService;
    @Autowired @Lazy private ModelEvaluationDatasetService modelEvaluationDatasetService;

    @Autowired
    public ModelEvaluationService(ModelEvaluationRepository modelEvaluationRepository,
                                  RoleCheckerService roleCheckerService) {
        this.modelEvaluationRepository = modelEvaluationRepository;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Starts a validation chain of a ModelEvaluation and all of its children for cascades
     *
     * @param studyId Id of the Study
     * @param modelEvaluationId Id of the ModelEvaluation
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateModelEvaluationDeletion(String studyId, String modelEvaluationId, Jwt principal) {
        List<ValidationResult> neighborResults = new ArrayList<>();

        neighborResults.add(evaluationMeasureService.validateCascade(studyId, "ModelEvaluation", modelEvaluationId, principal));
        neighborResults.add(modelEvaluationDatasetService.validateCascade(studyId, "ModelEvaluation", modelEvaluationId, principal));

        return ValidationResult.aggregate(neighborResults);
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
        List<ModelEvaluation> affectedEvaluations;

        switch (sourceResourceType) {
            case "Model":
                affectedEvaluations = modelEvaluationRepository.findByModelId(sourceResourceId);
                break;
            case "Organization":
                affectedEvaluations = modelEvaluationRepository.findByOrganizationId(sourceResourceId);
                break;
            default:
                return new ValidationResult(true, "");
        }

        if (affectedEvaluations.isEmpty()) {
            return new ValidationResult(true, "");
        }

        boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                studyId,
                principal,
                List.of(Role.DATA_SCIENTIST)
        );

        if (!hasPermission) {
            return new ValidationResult(false, "ModelEvaluation");
        }

        List<ValidationResult> childResults = new ArrayList<>();
        for (ModelEvaluation modelEvaluation : affectedEvaluations) {
            childResults.add(validateModelEvaluationDeletion(studyId, modelEvaluation.getModelEvaluationId(), principal));
        }
        childResults.add(new ValidationResult(true, "ModelEvaluation"));

        return ValidationResult.aggregate(childResults);
    }

    /**
     * Find ModelEvaluations by modelId
     * @param modelId ID of the Model
     * @return
     */
    public List<ModelEvaluation> findModelEvaluationsByModelId(String modelId) {
        return modelEvaluationRepository.findByModelId(modelId);
    }

    /**
     * Find a ModelEvaluation by modelEvaluationId
     * @param modelEvaluationId ID of the ModelEvaluation
     * @return
     */
    public Optional<ModelEvaluation> findModelEvaluationById(String modelEvaluationId) {
        return modelEvaluationRepository.findById(modelEvaluationId);
    }

    /**
     * Save a ModelEvaluation
     * @param modelEvaluation ModelEvaluation to be saved
     * @return
     */
    public ModelEvaluation saveModelEvaluation(ModelEvaluation modelEvaluation) {
        if (modelEvaluation.getExecutedAt() == null) {
            modelEvaluation.setExecutedAt(Instant.now());
        }
        return modelEvaluationRepository.save(modelEvaluation);
    }

    /**
     * Update a ModelEvaluation
     * @param modelEvaluationId ID of the ModelEvaluation
     * @param updatedModelEvaluation ModelEvaluation to be updated
     * @return
     */
    public Optional<ModelEvaluation> updateModelEvaluation(String modelEvaluationId, ModelEvaluation updatedModelEvaluation) {
        Optional<ModelEvaluation> oldModelEvaluation = modelEvaluationRepository.findById(modelEvaluationId);
        if (oldModelEvaluation.isPresent()) {
            ModelEvaluation modelEvaluation = oldModelEvaluation.get();
            modelEvaluation.setModelId(updatedModelEvaluation.getModelId());
            modelEvaluation.setOrganizationId(updatedModelEvaluation.getOrganizationId());
            modelEvaluation.setTrigger(updatedModelEvaluation.getTrigger());
            modelEvaluation.setAggregationMethod(updatedModelEvaluation.getAggregationMethod());
            modelEvaluation.setExecutedAt(updatedModelEvaluation.getExecutedAt());
            modelEvaluation.setExecutedBy(updatedModelEvaluation.getExecutedBy());
            modelEvaluation.setDescription(updatedModelEvaluation.getDescription());
            ModelEvaluation savedModelEvaluation = modelEvaluationRepository.save(modelEvaluation);
            return Optional.of(savedModelEvaluation);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a ModelEvaluation
     * @param modelEvaluationId ID of ModelEvaluation to be deleted
     * @return
     */
    public Optional<ModelEvaluation> deleteModelEvaluation(String modelEvaluationId) {
        Optional<ModelEvaluation> existingModelEvaluation = modelEvaluationRepository.findById(modelEvaluationId);
        if (existingModelEvaluation.isPresent()) {
            modelEvaluationRepository.delete(existingModelEvaluation.get());
            return existingModelEvaluation;
        } else {
            return Optional.empty();
        }
    }
}
