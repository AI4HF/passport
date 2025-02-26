package io.passport.server.controller;

import io.passport.server.model.*;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.ModelDeploymentService;
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
 * Class which stores the generated HTTP requests related to model deployment operations.
 */
@RestController
@RequestMapping("/modelDeployment")
public class ModelDeploymentController {

    private static final Logger log = LoggerFactory.getLogger(ModelDeploymentController.class);

    private final String relationName = "Model Deployment";
    private final ModelDeploymentService modelDeploymentService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    private final List<Role> allowedRoles = List.of(Role.ML_ENGINEER);

    @Autowired
    public ModelDeploymentController(ModelDeploymentService modelDeploymentService,
                                     RoleCheckerService roleCheckerService,
                                     AuditLogBookService auditLogBookService) {
        this.modelDeploymentService = modelDeploymentService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Retrieves ModelDeployments, optionally filtered by environmentId or studyId.
     *
     * @param environmentId Optional environment ID to filter by
     * @param studyId       Optional study ID for authorization / filter
     * @param principal     Jwt principal containing user info
     * @return List of ModelDeployment objects
     */
    @GetMapping
    public ResponseEntity<List<ModelDeployment>> getModelDeployments(
            @RequestParam(required = false) String environmentId,
            @RequestParam(required = false) String studyId,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(
                studyId,
                principal,
                List.of(Role.ML_ENGINEER, Role.QUALITY_ASSURANCE_SPECIALIST))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ModelDeployment> modelDeployments;
        if (environmentId != null) {
            Optional<ModelDeployment> mdOpt = this.modelDeploymentService.findModelDeploymentByEnvironmentId(environmentId);
            modelDeployments = mdOpt.map(List::of).orElseGet(List::of);
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
     * Retrieves a single ModelDeployment by its deploymentId.
     *
     * @param studyId      ID of the study for authorization
     * @param deploymentId ID of the ModelDeployment
     * @param principal    Jwt principal containing user info
     * @return ModelDeployment or NOT_FOUND
     */
    @GetMapping("/{deploymentId}")
    public ResponseEntity<?> getModelDeployment(@RequestParam String studyId,
                                                @PathVariable String deploymentId,
                                                @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(
                studyId,
                principal,
                List.of(Role.ML_ENGINEER, Role.QUALITY_ASSURANCE_SPECIALIST))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<ModelDeployment> mdOpt = this.modelDeploymentService.findModelDeploymentByDeploymentId(deploymentId);
        return mdOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new ModelDeployment.
     *
     * @param studyId         ID of the study for authorization
     * @param modelDeployment ModelDeployment instance to create
     * @param principal       Jwt principal containing user info
     * @return Created ModelDeployment or BAD_REQUEST on error
     */
    @PostMapping
    public ResponseEntity<?> createModelDeployment(@RequestParam String studyId,
                                                   @RequestBody ModelDeployment modelDeployment,
                                                   @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            ModelDeployment saved = this.modelDeploymentService.saveModelDeployment(modelDeployment);
            if (saved.getDeploymentId() != null) {
                String recordId = saved.getDeploymentId().toString();
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
            log.error("Error creating ModelDeployment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Updates an existing ModelDeployment by deploymentId.
     *
     * @param studyId                ID of the study for authorization
     * @param deploymentId           ID of the ModelDeployment to update
     * @param updatedModelDeployment Updated details
     * @param principal              Jwt principal containing user info
     * @return Updated ModelDeployment or NOT_FOUND
     */
    @PutMapping("/{deploymentId}")
    public ResponseEntity<?> updateModelDeployment(@RequestParam String studyId,
                                                   @PathVariable String deploymentId,
                                                   @RequestBody ModelDeployment updatedModelDeployment,
                                                   @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<ModelDeployment> savedOpt = this.modelDeploymentService
                    .updateModelDeployment(deploymentId, updatedModelDeployment);

            if (savedOpt.isPresent()) {
                ModelDeployment saved = savedOpt.get();
                String recordId = saved.getDeploymentId().toString();
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
            log.error("Error updating ModelDeployment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Deletes a ModelDeployment by its deploymentId.
     *
     * @param studyId      ID of the study for authorization
     * @param deploymentId ID of the ModelDeployment to delete
     * @param principal    Jwt principal containing user info
     * @return OK if deleted, NOT_FOUND otherwise
     */
    @DeleteMapping("/{deploymentId}")
    public ResponseEntity<?> deleteModelDeployment(@RequestParam String studyId,
                                                   @PathVariable String deploymentId,
                                                   @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<ModelDeployment> deletedModelDeployment = this.modelDeploymentService.deleteModelDeployment(deploymentId);
            if (deletedModelDeployment.isPresent()) {
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.DELETE,
                        relationName,
                        deploymentId.toString(),
                        deletedModelDeployment.get()
                );
                return ResponseEntity.status(HttpStatus.OK).body(deletedModelDeployment.get());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error deleting ModelDeployment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
