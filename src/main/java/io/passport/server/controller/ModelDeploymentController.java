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
    /**
     * ModelDeployment service for deployment management
     */
    private final ModelDeploymentService modelDeploymentService;

    /**
     * Role checker service for authorization
     */
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public ModelDeploymentController(ModelDeploymentService modelDeploymentService, RoleCheckerService roleCheckerService) {
        this.modelDeploymentService = modelDeploymentService;
        this.roleCheckerService = roleCheckerService;
    }


    /**
     * Retrieves all model deployments or a specific model deployment by environment ID if provided.
     * @param environmentId Optional ID of the environment to filter model deployments.
     * @param principal KeycloakPrincipal object that holds access token
     * @return A response entity containing a list of model deployments and the total count in the headers.
     */
    @GetMapping()
    public ResponseEntity<List<ModelDeployment>> getModelDeployments(
            @RequestParam(required = false) Long environmentId,
            @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ModelDeployment> modelDeployments;

        if (environmentId != null) {
            Optional<ModelDeployment> modelDeployment = this.modelDeploymentService.findModelDeploymentByEnvironmentId(environmentId);
            modelDeployments = modelDeployment.map(List::of).orElseGet(List::of);
        } else {
            modelDeployments = this.modelDeploymentService.getAllModelDeployments();
        }

        long totalCount = modelDeployments.size();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(modelDeployments);
    }



    /**
     * Read a model deployment by deploymentId
     * @param deploymentId ID of the model deployment
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping("/{deploymentId}")
    public ResponseEntity<?> getModelDeployment(@PathVariable Long deploymentId,
                                                @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<ModelDeployment> modelDeployment = this.modelDeploymentService.findModelDeploymentByDeploymentId(deploymentId);

        if(modelDeployment.isPresent()) {
            return ResponseEntity.ok().body(modelDeployment.get());
        }else{
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * Create ModelDeployment.
     * @param modelDeployment ModelDeployment model instance to be created.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createModelDeployment(@RequestBody ModelDeployment modelDeployment,
                                                   @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            ModelDeployment savedModelDeployment = this.modelDeploymentService.saveModelDeployment(modelDeployment);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedModelDeployment);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    /**
     * Update ModelDeployment.
     * @param deploymentId ID of the model deployment that is to be updated.
     * @param updatedModelDeployment ModelDeployment model instance with updated details.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PutMapping("/{deploymentId}")
    public ResponseEntity<?> updateModelDeployment(@PathVariable Long deploymentId,
                                                   @RequestBody ModelDeployment updatedModelDeployment,
                                                   @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<ModelDeployment> savedModelDeployment = this.modelDeploymentService.updateModelDeployment(deploymentId, updatedModelDeployment);
            if(savedModelDeployment.isPresent()) {
                return ResponseEntity.ok().body(savedModelDeployment);
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    /**
     * Delete by deployment ID.
     * @param deploymentId ID of the model deployment that is to be deleted.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @DeleteMapping("/{deploymentId}")
    public ResponseEntity<?> deleteModelDeployment(@PathVariable Long deploymentId,
                                                   @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.modelDeploymentService.deleteModelDeployment(deploymentId);
            if(isDeleted) {
                return ResponseEntity.noContent().build();
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
