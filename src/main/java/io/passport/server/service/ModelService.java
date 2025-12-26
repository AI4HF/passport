package io.passport.server.service;

import io.passport.server.model.Model;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.ModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for model management.
 */
@Service
public class ModelService {

    /**
     * Model repo access for database management.
     */
    private final ModelRepository modelRepository;
    private final RoleCheckerService roleCheckerService;

    /**
     * Lazy service references for limited use in cascade validation
     */
    @Autowired @Lazy private ModelDeploymentService modelDeploymentService;
    @Autowired @Lazy private ModelParameterService modelParameterService;
    @Autowired @Lazy private ModelFigureService modelFigureService;
    @Autowired @Lazy private EvaluationMeasureService evaluationMeasureService;

    @Autowired
    public ModelService(ModelRepository modelRepository, RoleCheckerService roleCheckerService) {
        this.modelRepository = modelRepository;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Starts a validation chain of Model and all of their children for cascades
     *
     * @param studyId Id of the Study
     * @param modelId Id of the Model
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateModelDeletion(String studyId, String modelId, Jwt principal) {
        List<ValidationResult> neighborResults = new ArrayList<>();

        neighborResults.add(modelDeploymentService.validateCascade(studyId, "Model", modelId, principal));
        neighborResults.add(modelParameterService.validateCascade(studyId, "Model", modelId, principal));
        neighborResults.add(modelFigureService.validateCascade(studyId, "Model", modelId, principal));
        neighborResults.add(evaluationMeasureService.validateCascade(studyId, "Model", modelId, principal));

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
        List<Model> affectedModels;

        switch (sourceResourceType) {
            case "LearningProcess":
                affectedModels = modelRepository.findByLearningProcessId(sourceResourceId);
                break;
            case "Experiment":
                affectedModels = modelRepository.findByExperimentId(sourceResourceId);
                break;
            case "Organization":
                affectedModels = modelRepository.findByOwner(sourceResourceId);
                break;
            default:
                return new ValidationResult(1, "");
        }

        if (affectedModels.isEmpty()) {
            return new ValidationResult(1, "");
        }

        List<ValidationResult> childResults = new ArrayList<>();
        boolean authorizedForModels = true;

        for (Model model : affectedModels) {
            boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                    model.getStudyId(),
                    principal,
                    List.of(Role.DATA_SCIENTIST)
            );

            if (!hasPermission) {
                authorizedForModels = false;
                break;
            }

            childResults.add(validateModelDeletion(model.getStudyId(), model.getModelId(), principal));
        }

        if (!authorizedForModels) {
            return new ValidationResult(0, "Model");
        }

        childResults.add(new ValidationResult(1, "Model"));

        return ValidationResult.aggregate(childResults);
    }

    public Optional<Model> findModelById(String modelId) {
        return modelRepository.findById(modelId);
    }

    /**
     * Save a model
     * @param model model to be saved
     * @return
     */
    public Model saveModel(Model model) {
        model.setCreatedAt(Instant.now());
        model.setLastUpdatedAt(Instant.now());
        return modelRepository.save(model);
    }

    /**
     * Update a model
     * @param modelId ID of the model
     * @param updatedModel model to be updated
     * @return
     */
    public Optional<Model> updateModel(String modelId, Model updatedModel) {
        Optional<Model> oldModel = modelRepository.findById(modelId);
        if (oldModel.isPresent()) {
            Model model = oldModel.get();
            model.setModelId(modelId);
            model.setLearningProcessId(updatedModel.getLearningProcessId());
            model.setExperimentId(updatedModel.getExperimentId());
            model.setName(updatedModel.getName());
            model.setVersion(updatedModel.getVersion());
            model.setTag(updatedModel.getTag());
            model.setModelType(updatedModel.getModelType());
            model.setProductIdentifier(updatedModel.getProductIdentifier());
            model.setTrlLevel(updatedModel.getTrlLevel());
            model.setLicense(updatedModel.getLicense());
            model.setPrimaryUse(updatedModel.getPrimaryUse());
            model.setSecondaryUse(updatedModel.getSecondaryUse());
            model.setIntendedUsers(updatedModel.getIntendedUsers());
            model.setCounterIndications(updatedModel.getCounterIndications());
            model.setEthicalConsiderations(updatedModel.getEthicalConsiderations());
            model.setLimitations(updatedModel.getLimitations());
            model.setFairnessConstraints(updatedModel.getFairnessConstraints());
            model.setLastUpdatedAt(Instant.now());
            model.setLastUpdatedBy(model.getLastUpdatedBy());
            Model savedModel = modelRepository.save(model);
            return Optional.of(savedModel);
        }else{
            return Optional.empty();
        }
    }

    /**
     * Delete a model
     * @param modelId ID of model to be deleted
     * @return
     */
    public Optional<Model> deleteModel(String modelId) {
        Optional<Model> existingModel = modelRepository.findById(modelId);
        if (existingModel.isPresent()) {
            modelRepository.delete(existingModel.get());
            return existingModel;
        } else {
            return Optional.empty();
        }
    }
    public List<Model> getAllModelsByStudyId(String studyId){
        return modelRepository.findByStudyId(studyId);
    }

    /**
     * Find Models created or last updated by a specific personnel.
     */
    public List<Model> findByCreatedByOrLastUpdatedBy(String personnelId) {
        return modelRepository.findByCreatedByOrLastUpdatedBy(personnelId);
    }

    /**
     * Resolve the Study ID for a given Model ID directly via DB query.
     */
    public Optional<String> findStudyIdByModelId(String modelId) {
        return modelRepository.findStudyIdByModelId(modelId);
    }
}