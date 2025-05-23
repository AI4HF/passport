package io.passport.server.controller;

import io.passport.server.model.*;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.FeatureSetService;
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
 * Class which stores the generated HTTP requests related to FeatureSet operations.
 */
@RestController
@RequestMapping("/featureset")
public class FeatureSetController {

    private static final Logger log = LoggerFactory.getLogger(FeatureSetController.class);

    private final String relationName = "Feature Set";
    private final FeatureSetService featureSetService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);

    @Autowired
    public FeatureSetController(FeatureSetService featureSetService,
                                RoleCheckerService roleCheckerService,
                                AuditLogBookService auditLogBookService) {
        this.featureSetService = featureSetService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Reads all FeatureSets by the given studyId.
     *
     * @param studyId   ID of the study
     * @param principal Jwt principal containing user info
     * @return List of FeatureSets or FORBIDDEN if not authorized
     */
    @GetMapping
    public ResponseEntity<List<FeatureSet>> getAllFeatureSetsByStudyId(@RequestParam String studyId,
                                                                       @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<FeatureSet> featureSets = this.featureSetService.getAllFeatureSetsByStudyId(studyId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(featureSets.size()));
        return ResponseEntity.ok().headers(headers).body(featureSets);
    }

    /**
     * Reads a single FeatureSet by its featureSetId.
     *
     * @param featureSetId ID of the FeatureSet
     * @param studyId      ID of the study for authorization
     * @param principal    Jwt principal containing user info
     * @return FeatureSet or NOT_FOUND
     */
    @GetMapping("/{featureSetId}")
    public ResponseEntity<?> getFeatureSet(@PathVariable String featureSetId,
                                           @RequestParam String studyId,
                                           @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<FeatureSet> fsOpt = this.featureSetService.findFeatureSetByFeatureSetId(featureSetId);
        return fsOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new FeatureSet.
     *
     * @param featureSet The FeatureSet model to create
     * @param studyId    ID of the study for authorization
     * @param principal  Jwt principal containing user info
     * @return Created FeatureSet or BAD_REQUEST on error
     */
    @PostMapping
    public ResponseEntity<?> createFeatureSet(@RequestBody FeatureSet featureSet,
                                              @RequestParam String studyId,
                                              @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            FeatureSet saved = this.featureSetService.saveFeatureSet(featureSet);
            if (saved.getFeaturesetId() != null) {
                String recordId = saved.getFeaturesetId();
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
            log.error("Error creating FeatureSet: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Updates an existing FeatureSet by featureSetId.
     *
     * @param featureSetId      ID of the FeatureSet to update
     * @param updatedFeatureSet Updated FeatureSet data
     * @param studyId           ID of the study for authorization
     * @param principal         Jwt principal containing user info
     * @return Updated FeatureSet or NOT_FOUND
     */
    @PutMapping("/{featureSetId}")
    public ResponseEntity<?> updateFeatureSet(@PathVariable String featureSetId,
                                              @RequestBody FeatureSet updatedFeatureSet,
                                              @RequestParam String studyId,
                                              @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<FeatureSet> savedOpt = this.featureSetService.updateFeatureSet(featureSetId, updatedFeatureSet);
            if (savedOpt.isPresent()) {
                FeatureSet saved = savedOpt.get();
                String recordId = saved.getFeaturesetId();
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
            log.error("Error updating FeatureSet: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Deletes a FeatureSet by its featureSetId.
     *
     * @param featureSetId ID of the FeatureSet to delete
     * @param studyId      ID of the study for authorization
     * @param principal    Jwt principal containing user info
     * @return OK if deleted, NOT_FOUND otherwise
     */
    @DeleteMapping("/{featureSetId}")
    public ResponseEntity<?> deleteFeatureSet(@PathVariable String featureSetId,
                                              @RequestParam String studyId,
                                              @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<FeatureSet> deletedFeatureSet = this.featureSetService.deleteFeatureSet(featureSetId);
            if (deletedFeatureSet.isPresent()) {
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId, Operation.DELETE,
                        relationName,
                        featureSetId,
                        deletedFeatureSet.get()
                );
                return ResponseEntity.status(HttpStatus.OK).body(deletedFeatureSet.get());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error deleting FeatureSet: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
