package io.passport.server.controller;

import io.passport.server.model.*;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.ModelService;
import io.passport.server.service.RoleCheckerService;
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

    private final String relationName = "Model";
    private final ModelService modelService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    private final List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);

    @Autowired
    public ModelController(ModelService modelService,
                           RoleCheckerService roleCheckerService,
                           AuditLogBookService auditLogBookService) {
        this.modelService = modelService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Retrieves all models by the given studyId.
     *
     * @param studyId   ID of the study
     * @param principal Jwt principal containing user info
     * @return List of Model objects
     */
    @GetMapping
    public ResponseEntity<List<Model>> getAllModels(@RequestParam Long studyId,
                                                    @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(
                studyId,
                principal,
                List.of(Role.DATA_SCIENTIST, Role.ML_ENGINEER, Role.QUALITY_ASSURANCE_SPECIALIST))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Model> models = modelService.getAllModelsByStudyId(studyId);
        return ResponseEntity.ok(models);
    }

    /**
     * Retrieves a single Model by its modelId.
     *
     * @param studyId   ID of the study for authorization
     * @param modelId   ID of the Model
     * @param principal Jwt principal containing user info
     * @return The requested Model or NOT_FOUND
     */
    @GetMapping("/{modelId}")
    public ResponseEntity<?> getModelById(@RequestParam Long studyId,
                                          @PathVariable Long modelId,
                                          @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(
                studyId,
                principal,
                List.of(Role.DATA_SCIENTIST, Role.QUALITY_ASSURANCE_SPECIALIST))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Model> modelOpt = this.modelService.findModelById(modelId);
        return modelOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new Model.
     *
     * @param studyId   ID of the study for authorization
     * @param model     Model to be created
     * @param principal Jwt principal containing user info
     * @return Created Model or BAD_REQUEST on error
     */
    @PostMapping
    public ResponseEntity<?> createModel(@RequestParam Long studyId,
                                         @RequestBody Model model,
                                         @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Model saved = this.modelService.saveModel(model);
            if (saved.getModelId() != null) {
                String recordId = saved.getModelId().toString();
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.CREATE,
                        relationName,
                        recordId,
                        saved
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {
            log.error("Error creating Model: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Updates an existing Model by modelId.
     *
     * @param studyId      ID of the study for authorization
     * @param modelId      ID of the Model to update
     * @param updatedModel Updated details
     * @param principal    Jwt principal containing user info
     * @return Updated Model or NOT_FOUND
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

            Optional<Model> savedOpt = this.modelService.updateModel(modelId, updatedModel);
            if (savedOpt.isPresent()) {
                Model saved = savedOpt.get();
                String recordId = saved.getModelId().toString();
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.UPDATE,
                        relationName,
                        recordId,
                        saved
                );
                return ResponseEntity.ok(saved);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating Model: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Deletes a Model by modelId.
     *
     * @param studyId   ID of the study for authorization
     * @param modelId   ID of the Model to delete
     * @param principal Jwt principal containing user info
     * @return OK if deleted, NOT_FOUND otherwise
     */
    @DeleteMapping("/{modelId}")
    public ResponseEntity<?> deleteModel(@RequestParam Long studyId,
                                         @PathVariable Long modelId,
                                         @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<Model> deletedModel = this.modelService.deleteModel(modelId);
            if (deletedModel.isPresent()) {
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.DELETE,
                        relationName,
                        modelId.toString(),
                        deletedModel.get()
                );
                return ResponseEntity.status(HttpStatus.OK).body(deletedModel.get());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error deleting Model: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
