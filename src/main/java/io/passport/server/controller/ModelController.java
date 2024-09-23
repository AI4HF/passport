package io.passport.server.controller;

import io.passport.server.model.Model;
import io.passport.server.model.Role;
import io.passport.server.service.ModelService;
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
 * Class which stores the generated HTTP requests related to model operations.
 */
@RestController
@RequestMapping("/model")
public class ModelController {

    private static final Logger log = LoggerFactory.getLogger(ModelController.class);

    /**
     * Model service for model management.
     */
    private final ModelService modelService;

    /**
     * Role checker service for authorization
     */
    private final RoleCheckerService roleCheckerService;

    /**
     * List of authorized roles for this endpoint
     */
    private final List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);

    @Autowired
    public ModelController(ModelService modelService, RoleCheckerService roleCheckerService) {
        this.modelService = modelService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read models, if studyId is provided, filter by studyId; otherwise, return all models.
     * @param studyId Optional ID of the study.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<Model>> getAllModels(@RequestParam(value = "studyId", required = false) Long studyId,
                                                    @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Model> models;

        if (studyId != null) {
            models = modelService.getAllModelsByStudyId(studyId);
        } else {
            models = modelService.getAllModels();
        }

        return ResponseEntity.ok(models);
    }

    /**
     * Read a model by id
     * @param modelId ID of the model
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping("/{modelId}")
    public ResponseEntity<?> getModelById(@PathVariable Long modelId,
                                          @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Model> model = this.modelService.findModelById(modelId);

        if (model.isPresent()) {
            return ResponseEntity.ok().body(model);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create a Model.
     * @param model model instance to be created.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createModel(@RequestBody Model model,
                                         @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Model savedModel = this.modelService.saveModel(model);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedModel);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Model.
     * @param modelId ID of the model that is to be updated.
     * @param updatedModel model instance with updated details.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PutMapping("/{modelId}")
    public ResponseEntity<?> updateModel(@PathVariable Long modelId,
                                         @RequestBody Model updatedModel,
                                         @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<Model> savedModel = this.modelService.updateModel(modelId, updatedModel);
            if (savedModel.isPresent()) {
                return ResponseEntity.ok().body(savedModel.get());
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete a model by Model ID.
     * @param modelId ID of the model that is to be deleted.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @DeleteMapping("/{modelId}")
    public ResponseEntity<?> deleteModel(@PathVariable Long modelId,
                                         @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.modelService.deleteModel(modelId);
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
