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
     * Return all model deployments
     * @return
     */
    public List<ModelDeployment> getAllModelDeployments() {
        return modelDeploymentRepository.findAll();
    }



    /**
     * Find a model deployment by deploymentId
     * @param deploymentId ID of the model deployment
     * @return
     */
    public Optional<ModelDeployment> findModelDeploymentByDeploymentId(Long deploymentId) {
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
    public Optional<ModelDeployment> updateModelDeployment(Long deploymentId, ModelDeployment updatedModelDeployment) {
        Optional<ModelDeployment> oldModelDeployment = modelDeploymentRepository.findById(deploymentId);
        if (oldModelDeployment.isPresent()) {
            ModelDeployment modelDeployment = oldModelDeployment.get();

            // set last updated timestamp
            modelDeployment.setLastUpdatedAt(Instant.now());

            // set remaining fields
            modelDeployment.setModelId(updatedModelDeployment.getModelId());
            modelDeployment.setEnvironmentId(updatedModelDeployment.getEnvironmentId());
            modelDeployment.setTags(updatedModelDeployment.getTags());
            modelDeployment.setIdentifiedFailures(updatedModelDeployment.getIdentifiedFailures());
            modelDeployment.setStatus(updatedModelDeployment.getStatus());
            modelDeployment.setCreatedBy(updatedModelDeployment.getCreatedBy());
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
    public boolean deleteModelDeployment(Long deploymentId) {
        Optional<ModelDeployment> modelDeployment = modelDeploymentRepository.findById(deploymentId);
        if(modelDeployment.isPresent()) {
            modelDeploymentRepository.deleteById(deploymentId);
            deploymentEnvironmentService.deleteDeploymentEnvironment(modelDeployment.get().getEnvironmentId());
            return true;
        }else{
            return false;
        }
    }

}
