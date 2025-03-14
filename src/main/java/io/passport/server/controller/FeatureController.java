package io.passport.server.controller;

import io.passport.server.model.*;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.FeatureService;
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
 * Class which stores the generated HTTP requests related to Feature operations.
 */
@RestController
@RequestMapping("/feature")
public class FeatureController {

    private static final Logger log = LoggerFactory.getLogger(FeatureController.class);

    private final String relationName = "Feature";
    private final FeatureService featureService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);

    @Autowired
    public FeatureController(FeatureService featureService,
                             RoleCheckerService roleCheckerService,
                             AuditLogBookService auditLogBookService) {
        this.featureService = featureService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Read all Features or filtered by featuresetId.
     *
     * @param featuresetId Optional ID of the FeatureSet
     * @param studyId      ID of the study for authorization
     * @param principal    Jwt principal containing user info
     * @return List of Features
     */
    @GetMapping
    public ResponseEntity<List<Feature>> getFeatures(
            @RequestParam(required = false) String featuresetId,
            @RequestParam String studyId,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Feature> features = (featuresetId != null)
                ? this.featureService.findByFeaturesetId(featuresetId)
                : this.featureService.getAllFeatures();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(features.size()));
        return ResponseEntity.ok().headers(headers).body(features);
    }

    /**
     * Read a Feature by id.
     *
     * @param featureId ID of the Feature
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return The requested Feature or NOT FOUND
     */
    @GetMapping("/{featureId}")
    public ResponseEntity<?> getFeature(@PathVariable String featureId,
                                        @RequestParam String studyId,
                                        @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Feature> featureOpt = this.featureService.findFeatureByFeatureId(featureId);
        return featureOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create a new Feature.
     *
     * @param feature   Feature model instance to be created
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return The created Feature or BAD_REQUEST on error
     */
    @PostMapping
    public ResponseEntity<?> createFeature(@RequestBody Feature feature,
                                           @RequestParam String studyId,
                                           @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Feature saved = this.featureService.saveFeature(feature);
            if (saved.getFeatureId() != null) {
                String recordId = saved.getFeatureId();
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
            log.error("Error creating Feature: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update an existing Feature by featureId.
     *
     * @param featureId      ID of the Feature to update
     * @param updatedFeature Updated details
     * @param studyId        ID of the study for authorization
     * @param principal      Jwt principal containing user info
     * @return Updated Feature or NOT_FOUND
     */
    @PutMapping("/{featureId}")
    public ResponseEntity<?> updateFeature(@PathVariable String featureId,
                                           @RequestBody Feature updatedFeature,
                                           @RequestParam String studyId,
                                           @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<Feature> savedOpt = this.featureService.updateFeature(featureId, updatedFeature);
            if (savedOpt.isPresent()) {
                Feature saved = savedOpt.get();
                String recordId = saved.getFeatureId();
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
            log.error("Error updating Feature: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete a Feature by featureId.
     *
     * @param featureId ID of the Feature to delete
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return OK if deleted, NOT_FOUND otherwise
     */
    @DeleteMapping("/{featureId}")
    public ResponseEntity<?> deleteFeature(@PathVariable String featureId,
                                           @RequestParam String studyId,
                                           @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<Feature> deletedFeature = this.featureService.deleteFeature(featureId);
            if (deletedFeature.isPresent()) {
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.DELETE,
                        relationName,
                        featureId,
                        deletedFeature.get()
                );
                return ResponseEntity.status(HttpStatus.OK).body(deletedFeature.get());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error deleting Feature: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
