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
import org.springframework.security.oauth2.jwt.Jwt;
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

    private final DeploymentEnvironmentService deploymentEnvironmentService;
    private final RoleCheckerService roleCheckerService;
    private final List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);

    @Autowired
    public DeploymentEnvironmentController(DeploymentEnvironmentService deploymentEnvironmentService, RoleCheckerService roleCheckerService) {
        this.deploymentEnvironmentService = deploymentEnvironmentService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read DeploymentEnvironment by environmentId
     * @param environmentId ID of the deployment environment.
     * @param studyId ID of the study
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping("/{environmentId}")
    public ResponseEntity<?> getDeploymentEnvironmentByEnvironmentId(
            @PathVariable("environmentId") Long environmentId,
            @RequestParam Long studyId,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<DeploymentEnvironment> deploymentEnvironment = this.deploymentEnvironmentService.findDeploymentEnvironmentById(environmentId);
        return deploymentEnvironment.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create a Deployment environment.
     * @param deploymentEnvironment DeploymentEnvironment model instance to be created.
     * @param studyId ID of the study
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createDeploymentEnvironment(@RequestBody DeploymentEnvironment deploymentEnvironment,
                                                         @RequestParam Long studyId,
                                                         @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            DeploymentEnvironment savedDevelopmentEnvironment = this.deploymentEnvironmentService
                    .saveDevelopmentEnvironment(deploymentEnvironment);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDevelopmentEnvironment);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Deployment environment.
     * @param deploymentEnvironmentId ID of the deployment environment that is to be updated.
     * @param updatedDeploymentEnvironment DeploymentEnvironment model instance with updated details.
     * @param studyId ID of the study
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PutMapping("/{deploymentEnvironmentId}")
    public ResponseEntity<?> updateDeploymentEnvironment(@PathVariable Long deploymentEnvironmentId,
                                                         @RequestBody DeploymentEnvironment updatedDeploymentEnvironment,
                                                         @RequestParam Long studyId,
                                                         @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<DeploymentEnvironment> savedDeploymentEnvironment = this.deploymentEnvironmentService.updateDeploymentEnvironment(deploymentEnvironmentId, updatedDeploymentEnvironment);
            return savedDeploymentEnvironment.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete a deployment environment by DeploymentEnvironment ID.
     * @param deploymentEnvironmentId ID of the deployment environment that is to be deleted.
     * @param studyId ID of the study
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @DeleteMapping("/{deploymentEnvironmentId}")
    public ResponseEntity<?> deletePersonnel(@PathVariable Long deploymentEnvironmentId,
                                             @RequestParam Long studyId,
                                             @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.deploymentEnvironmentService.deleteDeploymentEnvironment(deploymentEnvironmentId);
            return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

