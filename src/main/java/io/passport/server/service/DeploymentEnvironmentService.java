package io.passport.server.service;

import io.passport.server.model.DeploymentEnvironment;
import io.passport.server.repository.DeploymentEnvironmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service class for deployment environment management.
 */
@Service
public class DeploymentEnvironmentService {

    /**
     * DeploymentEnvironment repo access for database management.
     */
    private final DeploymentEnvironmentRepository deploymentEnvironmentRepository;

    @Autowired
    public DeploymentEnvironmentService(DeploymentEnvironmentRepository deploymentEnvironmentRepository) {
        this.deploymentEnvironmentRepository = deploymentEnvironmentRepository;
    }


    /**
     * Find a DeploymentEnvironment by environmentId
     * @param environmentId ID of the deployment environment
     * @return
     */
    public Optional<DeploymentEnvironment> findDeploymentEnvironmentById(String environmentId) {
        return deploymentEnvironmentRepository.findById(environmentId);
    }




    /**
     * Save a deployment environment
     * @param deploymentEnvironment deploymentEnvironment to be saved
     * @return
     */
    public DeploymentEnvironment saveDevelopmentEnvironment(DeploymentEnvironment deploymentEnvironment) {
        return deploymentEnvironmentRepository.save(deploymentEnvironment);
    }



    /**
     * Update a deployment environment
     * @param deploymentEnvironmentId ID of the environment
     * @param updatedDeploymentEnvironment environment to be updated
     * @return
     */
    public Optional<DeploymentEnvironment> updateDeploymentEnvironment(String deploymentEnvironmentId, DeploymentEnvironment updatedDeploymentEnvironment) {
        Optional<DeploymentEnvironment> oldDeploymentEnvironment = deploymentEnvironmentRepository.findById(deploymentEnvironmentId);
        if (oldDeploymentEnvironment.isPresent()) {
            DeploymentEnvironment deploymentEnvironment = oldDeploymentEnvironment.get();
            deploymentEnvironment.setTitle(updatedDeploymentEnvironment.getTitle());
            deploymentEnvironment.setDescription(updatedDeploymentEnvironment.getDescription());
            deploymentEnvironment.setHardwareProperties(updatedDeploymentEnvironment.getHardwareProperties());
            deploymentEnvironment.setSoftwareProperties(updatedDeploymentEnvironment.getSoftwareProperties());
            deploymentEnvironment.setConnectivityDetails(updatedDeploymentEnvironment.getConnectivityDetails());
            DeploymentEnvironment savedDeploymentEnvironment = deploymentEnvironmentRepository.save(deploymentEnvironment);
            return Optional.of(savedDeploymentEnvironment);
        } else {
            return Optional.empty();
        }
    }


    /**
     * Delete a deployment environment
     * @param deploymentEnvironmentId ID of deployment environment to be deleted
     * @return
     */
    public Optional<DeploymentEnvironment> deleteDeploymentEnvironment(String deploymentEnvironmentId) {
        Optional<DeploymentEnvironment> existingEnvironment = deploymentEnvironmentRepository.findById(deploymentEnvironmentId);
        if (existingEnvironment.isPresent()) {
            deploymentEnvironmentRepository.delete(existingEnvironment.get());
            return existingEnvironment;
        } else {
            return Optional.empty();
        }
    }

}
