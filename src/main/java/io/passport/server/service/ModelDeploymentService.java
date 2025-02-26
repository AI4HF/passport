package io.passport.server.service;

import io.passport.server.model.ModelDeployment;
import io.passport.server.repository.ModelDeploymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
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


    @Autowired
    public ModelDeploymentService(ModelDeploymentRepository modelDeploymentRepository, DeploymentEnvironmentService deploymentEnvironmentService) {
        this.modelDeploymentRepository = modelDeploymentRepository;
        this.deploymentEnvironmentService = deploymentEnvironmentService;
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


}
