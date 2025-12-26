package io.passport.server.service;

import io.passport.server.model.ModelDeployment;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.ModelDeploymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for model deployment management.
 */
@Service
public class ModelDeploymentService {

    /**
     * ModelDeployment repo access for database management.
     */
    private final ModelDeploymentRepository modelDeploymentRepository;

    /**
     * Deployment Environment service access.
     */
    private final DeploymentEnvironmentService deploymentEnvironmentService;
    private final RoleCheckerService roleCheckerService;

    /**
     * Lazy service references for limited use in cascade validation
     */
    @Autowired @Lazy private PassportService passportService;

    @Autowired
    public ModelDeploymentService(ModelDeploymentRepository modelDeploymentRepository,
                                  DeploymentEnvironmentService deploymentEnvironmentService,
                                  RoleCheckerService roleCheckerService) {
        this.modelDeploymentRepository = modelDeploymentRepository;
        this.deploymentEnvironmentService = deploymentEnvironmentService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Starts a validation chain of Model Deployment and all of their children for cascades
     *
     * @param studyId Id of the Study
     * @param deploymentId Id of the Model Deployment
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateModelDeploymentDeletion(String studyId, String deploymentId, Jwt principal) {
        List<ValidationResult> results = new ArrayList<>();

        results.add(passportService.validateCascade(studyId, "ModelDeployment", deploymentId, principal));
        results.add(new ValidationResult(1, "DeploymentEnvironment"));

        return ValidationResult.aggregate(results);
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
        List<ModelDeployment> affectedDeployments;

        switch (sourceResourceType) {
            case "Model":
                affectedDeployments = modelDeploymentRepository.findByModelId(sourceResourceId);
                break;
            default:
                return new ValidationResult(1, "");
        }

        if (affectedDeployments.isEmpty()) {
            return new ValidationResult(1, "");
        }

        List<ValidationResult> childResults = new ArrayList<>();
        boolean authorizedForDeployments = true;

        for (ModelDeployment deployment : affectedDeployments) {
            boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                    studyId,
                    principal,
                    List.of(Role.ML_ENGINEER)
            );

            if (!hasPermission) {
                authorizedForDeployments = false;
                break;
            }

            childResults.add(validateModelDeploymentDeletion(studyId, deployment.getDeploymentId(), principal));
        }

        if (!authorizedForDeployments) {
            return new ValidationResult(0, "ModelDeployment");
        }

        childResults.add(new ValidationResult(1, "ModelDeployment"));

        return ValidationResult.aggregate(childResults);
    }

    /**
     * Return all model deployments by studyId
     * @param studyId ID of the study
     * @return
     */
    public List<ModelDeployment> getAllModelDeploymentsByStudyId(String studyId) {
        return modelDeploymentRepository.findAllByStudyId(studyId);
    }



    /**
     * Find a model deployment by environmentId
     * @param environmentId ID of the deployment environment
     * @return
     */
    public Optional<ModelDeployment> findModelDeploymentByEnvironmentId(String environmentId) {
        return modelDeploymentRepository.findByEnvironmentId(environmentId);
    }


    /**
     * Find a model deployment by deploymentId
     * @param deploymentId ID of the model deployment
     * @return
     */
    public Optional<ModelDeployment> findModelDeploymentByDeploymentId(String deploymentId) {
        return modelDeploymentRepository.findById(deploymentId);
    }


    /**
     * Save a model deployment
     * @param modelDeployment model deployment to be saved
     * @return
     */
    public ModelDeployment saveModelDeployment(ModelDeployment modelDeployment) {
        // Set the creation and last update time
        Instant now = Instant.now();
        modelDeployment.setCreatedAt(now);
        modelDeployment.setLastUpdatedAt(now);

        return modelDeploymentRepository.save(modelDeployment);
    }

    /**
     * Update a model deployment
     * @param deploymentId ID of the model deployment
     * @param updatedModelDeployment model deployment to be updated
     * @return
     */
    public Optional<ModelDeployment> updateModelDeployment(String deploymentId, ModelDeployment updatedModelDeployment) {
        Optional<ModelDeployment> oldModelDeployment = modelDeploymentRepository.findById(deploymentId);
        if (oldModelDeployment.isPresent()) {
            ModelDeployment modelDeployment = oldModelDeployment.get();

            modelDeployment.setLastUpdatedAt(Instant.now());
            modelDeployment.setModelId(updatedModelDeployment.getModelId());
            modelDeployment.setEnvironmentId(updatedModelDeployment.getEnvironmentId());
            modelDeployment.setTags(updatedModelDeployment.getTags());
            modelDeployment.setIdentifiedFailures(updatedModelDeployment.getIdentifiedFailures());
            modelDeployment.setStatus(updatedModelDeployment.getStatus());
            modelDeployment.setLastUpdatedBy(updatedModelDeployment.getLastUpdatedBy());

            ModelDeployment savedModelDeployment = modelDeploymentRepository.save(modelDeployment);
            return Optional.of(savedModelDeployment);
        } else {
            return Optional.empty();
        }
    }


    /**
     * Delete a model deployment
     * @param deploymentId ID of model deployment to be deleted
     * @return
     */
    public Optional<ModelDeployment> deleteModelDeployment(String deploymentId) {
        Optional<ModelDeployment> existingDeployment = modelDeploymentRepository.findById(deploymentId);
        if (existingDeployment.isPresent()) {
            modelDeploymentRepository.delete(existingDeployment.get());
            deploymentEnvironmentService.deleteDeploymentEnvironment(existingDeployment.get().getEnvironmentId());
            return existingDeployment;
        } else {
            return Optional.empty();
        }
    }

    /**
     * Find ModelDeployments created or last updated by a specific personnel.
     */
    public List<ModelDeployment> findByCreatedByOrLastUpdatedBy(String personnelId) {
        return modelDeploymentRepository.findByCreatedByOrLastUpdatedBy(personnelId);
    }

    /**
     * Resolve the Study ID for a given Deployment ID directly via DB query.
     */
    public Optional<String> findStudyIdByDeploymentId(String deploymentId) {
        return modelDeploymentRepository.findStudyIdByDeploymentId(deploymentId);
    }


}
