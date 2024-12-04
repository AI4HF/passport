package io.passport.server.controller;

import io.passport.server.model.ModelDeployment;
import io.passport.server.model.Role;
import io.passport.server.service.ModelDeploymentService;
import io.passport.server.service.RoleCheckerService;
import org.keycloak.KeycloakPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Class which stores the generated HTTP requests related to model deployment operations.
 */
@RestController
@RequestMapping("/modelDeployment")
public class ModelDeploymentController {

    private static final Logger log = LoggerFactory.getLogger(ModelDeploymentController.class);
    private final ModelDeploymentService modelDeploymentService;
    private final RoleCheckerService roleCheckerService;
    private final List<Role> allowedRoles = List.of(Role.ML_ENGINEER);

    @Autowired
    public ModelDeploymentController(ModelDeploymentService modelDeploymentService, RoleCheckerService roleCheckerService) {
        this.modelDeploymentService = modelDeploymentService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Retrieve model deployments filtered by environmentId or studyId.
     * @param environmentId Optional ID of the environment
     * @param studyId ID of the study for authorization
     * @param principal KeycloakPrincipal object that holds access token
     * @return List of ModelDeployments
     */
    @GetMapping()
    public ResponseEntity<List<ModelDeployment>> getModelDeployments(
            @RequestParam(required = false) Long environmentId,
            @RequestParam(required = false) Long studyId,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ModelDeployment> modelDeployments;

        if (environmentId != null) {
            Optional<ModelDeployment> modelDeployment = this.modelDeploymentService.findModelDeploymentByEnvironmentId(environmentId);
            modelDeployments = modelDeployment.map(List::of).orElseGet(List::of);
        } else if (studyId != null) {
            modelDeployments = this.modelDeploymentService.getAllModelDeploymentsByStudyId(studyId);
        } else {
            modelDeployments = List.of();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(modelDeployments.size()));

        return ResponseEntity.ok().headers(headers).body(modelDeployments);
    }

    /**
     * Read a ModelDeployment by ID.
     * @param studyId ID of the study for authorization
     * @param deploymentId ID of the model deployment
     * @param principal KeycloakPrincipal object that holds access token
     * @return ModelDeployment or not found status
     */
    @GetMapping("/{deploymentId}")
    public ResponseEntity<?> getModelDeployment(@RequestParam Long studyId,
                                                @PathVariable Long deploymentId,
                                                @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<ModelDeployment> modelDeployment = this.modelDeploymentService.findModelDeploymentByDeploymentId(deploymentId);

        return modelDeployment.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create a new ModelDeployment.
     * @param studyId ID of the study for authorization
     * @param modelDeployment ModelDeployment model instance to be created
     * @param principal KeycloakPrincipal object that holds access token
     * @return Created ModelDeployment
     */
    @PostMapping()
    public ResponseEntity<?> createModelDeployment(@RequestParam Long studyId,
                                                   @RequestBody ModelDeployment modelDeployment,
                                                   @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            ModelDeployment savedModelDeployment = this.modelDeploymentService.saveModelDeployment(modelDeployment);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedModelDeployment);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update ModelDeployment by ID.
     * @param studyId ID of the study for authorization
     * @param deploymentId ID of the model deployment
     * @param updatedModelDeployment ModelDeployment instance with updated details
     * @param principal KeycloakPrincipal object that holds access token
     * @return Updated ModelDeployment or not found status
     */
    @PutMapping("/{deploymentId}")
    public ResponseEntity<?> updateModelDeployment(@RequestParam Long studyId,
                                                   @PathVariable Long deploymentId,
                                                   @RequestBody ModelDeployment updatedModelDeployment,
                                                   @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<ModelDeployment> savedModelDeployment = this.modelDeploymentService.updateModelDeployment(deploymentId, updatedModelDeployment);
            return savedModelDeployment.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete ModelDeployment by ID.
     * @param studyId ID of the study for authorization
     * @param deploymentId ID of the model deployment
     * @param principal KeycloakPrincipal object that holds access token
     * @return No content or not found status
     */
    @DeleteMapping("/{deploymentId}")
    public ResponseEntity<?> deleteModelDeployment(@RequestParam Long studyId,
                                                   @PathVariable Long deploymentId,
                                                   @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.modelDeploymentService.deleteModelDeployment(deploymentId);
            return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
