package io.passport.server.controller;

import io.passport.server.model.*;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.DeploymentEnvironmentService;
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
 * Class which stores the generated HTTP requests related to DeploymentEnvironment operations.
 */
@RestController
@RequestMapping("/deploymentEnvironment")
public class DeploymentEnvironmentController {

    private static final Logger log = LoggerFactory.getLogger(DeploymentEnvironmentController.class);

    private final String relationName = "Deployment Environment";
    private final DeploymentEnvironmentService deploymentEnvironmentService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    private final List<Role> allowedRoles = List.of(Role.ML_ENGINEER);

    @Autowired
    public DeploymentEnvironmentController(DeploymentEnvironmentService deploymentEnvironmentService,
                                           RoleCheckerService roleCheckerService,
                                           AuditLogBookService auditLogBookService) {
        this.deploymentEnvironmentService = deploymentEnvironmentService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Retrieves a DeploymentEnvironment by its environmentId.
     *
     * @param environmentId ID of the deployment environment
     * @param studyId       ID of the study for authorization
     * @param principal     Jwt principal containing user info
     * @return The requested DeploymentEnvironment or NOT_FOUND
     */
    @GetMapping("/{environmentId}")
    public ResponseEntity<?> getDeploymentEnvironmentByEnvironmentId(
            @PathVariable("environmentId") Long environmentId,
            @RequestParam Long studyId,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<DeploymentEnvironment> envOpt = this.deploymentEnvironmentService.findDeploymentEnvironmentById(environmentId);
        return envOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new DeploymentEnvironment.
     *
     * @param deploymentEnvironment The environment to create
     * @param studyId               ID of the study for authorization
     * @param principal             Jwt principal containing user info
     * @return Created environment or BAD_REQUEST
     */
    @PostMapping
    public ResponseEntity<?> createDeploymentEnvironment(@RequestBody DeploymentEnvironment deploymentEnvironment,
                                                         @RequestParam Long studyId,
                                                         @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            DeploymentEnvironment saved = this.deploymentEnvironmentService
                    .saveDevelopmentEnvironment(deploymentEnvironment);

            if (saved.getEnvironmentId() != null) {
                String recordId = saved.getEnvironmentId().toString();
                String description = Description.CREATION.getDescription(relationName, recordId);
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.CREATE,
                        relationName,
                        recordId,
                        saved,
                        description
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {
            log.error("Error creating DeploymentEnvironment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Updates an existing DeploymentEnvironment by ID.
     *
     * @param deploymentEnvironmentId      ID of the environment to update
     * @param updatedDeploymentEnvironment Updated environment data
     * @param studyId                      ID of the study for authorization
     * @param principal                    Jwt principal containing user info
     * @return Updated environment or NOT_FOUND
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

            Optional<DeploymentEnvironment> savedOpt =
                    this.deploymentEnvironmentService.updateDeploymentEnvironment(deploymentEnvironmentId, updatedDeploymentEnvironment);

            if (savedOpt.isPresent()) {
                DeploymentEnvironment saved = savedOpt.get();
                String recordId = saved.getEnvironmentId().toString();
                String description = Description.UPDATE.getDescription(relationName, recordId);
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.UPDATE,
                        relationName,
                        recordId,
                        saved,
                        description
                );
                return ResponseEntity.ok(saved);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error updating DeploymentEnvironment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Deletes a DeploymentEnvironment by its ID.
     *
     * @param deploymentEnvironmentId ID of the environment to delete
     * @param studyId                 ID of the study for authorization
     * @param principal               Jwt principal containing user info
     * @return NO_CONTENT if deleted, NOT_FOUND otherwise
     */
    @DeleteMapping("/{deploymentEnvironmentId}")
    public ResponseEntity<?> deletePersonnel(@PathVariable Long deploymentEnvironmentId,
                                             @RequestParam Long studyId,
                                             @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<DeploymentEnvironment> deletedDeploymentEnvironment = this.deploymentEnvironmentService.deleteDeploymentEnvironment(deploymentEnvironmentId);
            if (deletedDeploymentEnvironment.isPresent()) {
                String description = Description.DELETION.getDescription(relationName, deploymentEnvironmentId.toString());
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.DELETE,
                        relationName,
                        deploymentEnvironmentId.toString(),
                        deletedDeploymentEnvironment.get(),
                        description
                );
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(deletedDeploymentEnvironment.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting DeploymentEnvironment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
