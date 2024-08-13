package io.passport.server.controller;

import io.passport.server.model.DeploymentEnvironment;
import io.passport.server.model.Role;
import io.passport.server.service.DeploymentEnvironmentService;
import io.passport.server.service.RoleCheckerService;
import org.keycloak.KeycloakPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    /**
     * Role checker service for authorization
     */
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public DeploymentEnvironmentController(DeploymentEnvironmentService deploymentEnvironmentService, RoleCheckerService roleCheckerService) {
        this.deploymentEnvironmentService = deploymentEnvironmentService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read DeploymentEnvironment by environmentId
     * @param environmentId ID of the deployment environment.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping("/{environmentId}")
    public ResponseEntity<?> getDeploymentEnvironmentByEnvironmentId(
            @PathVariable("environmentId") Long environmentId,
            @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

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
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createDeploymentEnvironment(@RequestBody DeploymentEnvironment deploymentEnvironment,
                                                         @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

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
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PutMapping("/{deploymentEnvironmentId}")
    public ResponseEntity<?> updateDeploymentEnvironment(@PathVariable Long deploymentEnvironmentId,
                                                         @RequestBody DeploymentEnvironment updatedDeploymentEnvironment,
                                                         @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

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
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @DeleteMapping("/{deploymentEnvironmentId}")
    public ResponseEntity<?> deletePersonnel(@PathVariable Long deploymentEnvironmentId,
                                             @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

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
