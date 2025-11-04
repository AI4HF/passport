package io.passport.server.controller;

import io.passport.server.model.*;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.ModelFigureService;
import io.passport.server.service.RoleCheckerService;
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
 * Class which stores the generated HTTP requests related to ModelFigure operations.
 */
@RestController
@RequestMapping("/model-figure")
public class ModelFigureController {

    private static final Logger log = LoggerFactory.getLogger(ModelFigureController.class);

    private final String relationName = "Model Figure";
    private final ModelFigureService modelFigureService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    private final List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);

    @Autowired
    public ModelFigureController(ModelFigureService modelFigureService,
                                    RoleCheckerService roleCheckerService,
                                    AuditLogBookService auditLogBookService) {
        this.modelFigureService = modelFigureService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Read all ModelFigures or filtered by modelId
     * @param studyId ID of the study for authorization
     * @param modelId ID of the Model (optional)
     * @param principal Jwt principal containing user info
     * @return List of ModelFigures
     */
    @GetMapping
    public ResponseEntity<List<ModelFigure>> getModelFigures(
            @RequestParam String studyId,
            @RequestParam(required = false) String modelId,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ModelFigure> figures;
        if (modelId != null) {
            figures = this.modelFigureService.findByModelId(modelId);
        }else {
            figures = this.modelFigureService.getAllModelFigures();
        }

        if (modelId != null && figures.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(figures.size()));
        return ResponseEntity.ok().headers(headers).body(figures);
    }

    /**
     * Create a new ModelFigure entity.
     * @param studyId ID of the study for authorization
     * @param modelFigure the new ModelFigure object
     * @param principal Jwt principal containing user info
     * @return Created ModelFigure
     */
    @PostMapping
    public ResponseEntity<?> createModelFigure(@RequestParam String studyId,
                                                  @RequestBody ModelFigure modelFigure,
                                                  @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            ModelFigure saved = this.modelFigureService.saveModelFigure(modelFigure);

            if (saved.getFigureId() != null) {
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.CREATE,
                        relationName,
                        saved.getFigureId(),
                        saved
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {
            log.error("Error creating ModelFigure: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update ModelFigure using query parameters.
     * @param studyId ID of the study for authorization
     * @param figureId ID of the ModelFigure
     * @param updatedModelFigure Updated details
     * @param principal Jwt principal containing user info
     * @return Updated ModelFigure or NOT_FOUND
     */
    @PutMapping
    public ResponseEntity<?> updateModelFigure(
            @RequestParam String studyId,
            @RequestParam String figureId,
            @RequestBody ModelFigure updatedModelFigure,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Optional<ModelFigure> savedOpt = this.modelFigureService.updateModelFigure(figureId, updatedModelFigure);
            if (savedOpt.isPresent()) {
                ModelFigure saved = savedOpt.get();
                if (saved.getFigureId() != null) {
                    auditLogBookService.createAuditLog(
                            principal.getSubject(),
                            principal.getClaim(TokenClaim.USERNAME.getValue()),
                            studyId,
                            Operation.UPDATE,
                            relationName,
                            saved.getFigureId(),
                            saved
                    );
                }
                return ResponseEntity.ok(saved);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error updating ModelFigure: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete ModelFigure by ID using query parameters.
     * @param studyId ID of the study for authorization
     * @param figureId ID of the ModelFigure
     * @param principal Jwt principal containing user info
     * @return No content or NOT_FOUND
     */
    @DeleteMapping
    public ResponseEntity<?> deleteModelFigure(
            @RequestParam String studyId,
            @RequestParam String figureId,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Optional<ModelFigure> deletedModelFigure = this.modelFigureService.deleteModelFigure(figureId);
            if (deletedModelFigure.isPresent()) {
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.DELETE,
                        relationName,
                        deletedModelFigure.get().getFigureId(),
                        deletedModelFigure.get()
                );
                return ResponseEntity.status(HttpStatus.OK).body(deletedModelFigure.get());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error deleting ModelFigure: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
