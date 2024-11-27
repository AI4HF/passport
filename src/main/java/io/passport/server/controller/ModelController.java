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
import org.springframework.security.oauth2.jwt.Jwt;
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
    private final ModelService modelService;
    private final RoleCheckerService roleCheckerService;
    private final List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);

    @Autowired
    public ModelController(ModelService modelService, RoleCheckerService roleCheckerService) {
        this.modelService = modelService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Retrieve all models, or filter by studyId if provided.
     * @param studyId ID of the study for authorization
     * @param principal KeycloakPrincipal object that holds access token
     * @return List of models
     */
    @GetMapping()
    public ResponseEntity<List<Model>> getAllModels(@RequestParam Long studyId,
                                                    @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Model> models = modelService.getAllModelsByStudyId(studyId);
        return ResponseEntity.ok(models);
    }

    /**
     * Retrieve a model by id
     * @param studyId ID of the study for authorization
     * @param modelId ID of the model
     * @param principal KeycloakPrincipal object that holds access token
     * @return Model or 404 if not found
     */
    @GetMapping("/{modelId}")
    public ResponseEntity<?> getModelById(@RequestParam Long studyId,
                                          @PathVariable Long modelId,
                                          @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Model> model = this.modelService.findModelById(modelId);
        return model.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create a new Model.
     * @param studyId ID of the study for authorization
     * @param model Model to be created
     * @param principal KeycloakPrincipal object that holds access token
     * @return ResponseEntity with created Model
     */
    @PostMapping()
    public ResponseEntity<?> createModel(@RequestParam Long studyId,
                                         @RequestBody Model model,
                                         @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Model savedModel = this.modelService.saveModel(model);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedModel);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Model by ID.
     * @param studyId ID of the study for authorization
     * @param modelId ID of the model to be updated
     * @param updatedModel Updated Model
     * @param principal KeycloakPrincipal object that holds access token
     * @return ResponseEntity with updated model or not found status
     */
    @PutMapping("/{modelId}")
    public ResponseEntity<?> updateModel(@RequestParam Long studyId,
                                         @PathVariable Long modelId,
                                         @RequestBody Model updatedModel,
                                         @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<Model> savedModel = this.modelService.updateModel(modelId, updatedModel);
            return savedModel.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete a model by Model ID.
     * @param studyId ID of the study for authorization
     * @param modelId ID of the model to be deleted
     * @param principal KeycloakPrincipal object that holds access token
     * @return ResponseEntity with no content status or not found status
     */
    @DeleteMapping("/{modelId}")
    public ResponseEntity<?> deleteModel(@RequestParam Long studyId,
                                         @PathVariable Long modelId,
                                         @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.modelService.deleteModel(modelId);
            return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
