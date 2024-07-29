package io.passport.server.controller;

import io.passport.server.model.DeploymentEnvironment;
import io.passport.server.service.DeploymentEnvironmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Class which stores the generated HTTP requests related to DeploymentEnvironment operations.
 */
@RestController
@RequestMapping("/deploymentEnvironment")
public class DeploymentEnvironmentController {

    private static final Logger log = LoggerFactory.getLogger(DeploymentEnvironmentController.class);

    /**
     * DeploymentEnvironment service for deploymentEnvironment management.
     */
    private final DeploymentEnvironmentService deploymentEnvironmentService;

    @Autowired
    public DeploymentEnvironmentController(DeploymentEnvironmentService deploymentEnvironmentService) {
        this.deploymentEnvironmentService = deploymentEnvironmentService;
    }



    /**
     * Read DeploymentEnvironment by environmentId
     * @param environmentId ID of the deployment environment.
     * @return
     */
    @GetMapping("/{environmentId}")
    public ResponseEntity<?> getDeploymentEnvironmentByEnvironmentId(
            @PathVariable("environmentId") Long environmentId) {

        Optional<DeploymentEnvironment> deploymentEnvironment = this.deploymentEnvironmentService.findDeploymentEnvironmentById(environmentId);

        if(deploymentEnvironment.isPresent()) {
            return ResponseEntity.ok().body(deploymentEnvironment);
        }else{
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * Create a Deployment environment.
     * @param deploymentEnvironment DeploymentEnvironment model instance to be created.
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createDeploymentEnvironment(@RequestBody DeploymentEnvironment deploymentEnvironment) {
        try{
            DeploymentEnvironment savedDevelopmentEnvironment = this.deploymentEnvironmentService
                                                .saveDevelopmentEnvironment(deploymentEnvironment);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDevelopmentEnvironment);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    /**
     * Update Deployment environment.
     * @param deploymentEnvironmentId ID of the deployment environment that is to be updated.
     * @param updatedDeploymentEnvironment DeploymentEnvironment model instance with updated details.
     * @return
     */
    @PutMapping("/{deploymentEnvironmentId}")
    public ResponseEntity<?> updateDeploymentEnvironment(@PathVariable Long deploymentEnvironmentId, @RequestBody DeploymentEnvironment updatedDeploymentEnvironment) {
        try{
            Optional<DeploymentEnvironment> savedDeploymentEnvironment = this.deploymentEnvironmentService.updateDeploymentEnvironment(deploymentEnvironmentId, updatedDeploymentEnvironment);
            if(savedDeploymentEnvironment.isPresent()) {
                return ResponseEntity.ok().body(savedDeploymentEnvironment.get());
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    /**
     * Delete a deployment environment by DeploymentEnvironment ID.
     * @param deploymentEnvironmentId ID of the deployment environment that is to be deleted.
     * @return
     */
    @DeleteMapping("/{deploymentEnvironmentId}")
    public ResponseEntity<?> deleteDeploymentEnvironment(@PathVariable Long deploymentEnvironmentId) {
        try{
            boolean isDeleted = this.deploymentEnvironmentService.deleteDeploymentEnvironment(deploymentEnvironmentId);
            if(isDeleted) {
                return ResponseEntity.noContent().build();
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


}
