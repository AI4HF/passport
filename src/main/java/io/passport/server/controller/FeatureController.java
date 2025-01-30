package io.passport.server.controller;

import io.passport.server.model.Feature;
import io.passport.server.model.Role;
import io.passport.server.service.FeatureService;
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
 * Class which stores the generated HTTP requests related to Feature operations.
 */
@RestController
@RequestMapping("/feature")
public class FeatureController {

    private static final Logger log = LoggerFactory.getLogger(FeatureController.class);

    private final FeatureService featureService;
    private final RoleCheckerService roleCheckerService;
    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);

    @Autowired
    public FeatureController(FeatureService featureService, RoleCheckerService roleCheckerService) {
        this.featureService = featureService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read all Features or filtered by featuresetId
     * @param featuresetId ID of the FeatureSet (optional)
     * @param studyId ID of the study
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<Feature>> getFeatures(
            @RequestParam(required = false) Long featuresetId,
            @RequestParam Long studyId,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Feature> features = (featuresetId != null) ?
                this.featureService.findByFeaturesetId(featuresetId) :
                this.featureService.getAllFeatures();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(features.size()));

        return ResponseEntity.ok().headers(headers).body(features);
    }

    /**
     * Read a Feature by id
     * @param featureId ID of the Feature
     * @param studyId ID of the study
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping("/{featureId}")
    public ResponseEntity<?> getFeature(@PathVariable Long featureId,
                                        @RequestParam Long studyId,
                                        @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Feature> feature = this.featureService.findFeatureByFeatureId(featureId);
        return feature.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create Feature.
     * @param feature Feature model instance to be created.
     * @param studyId ID of the study
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createFeature(@RequestBody Feature feature,
                                           @RequestParam Long studyId,
                                           @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Feature savedFeature = this.featureService.saveFeature(feature);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedFeature);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Feature.
     * @param featureId ID of the Feature that is to be updated.
     * @param updatedFeature Feature model instance with updated details.
     * @param studyId ID of the study
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PutMapping("/{featureId}")
    public ResponseEntity<?> updateFeature(@PathVariable Long featureId,
                                           @RequestBody Feature updatedFeature,
                                           @RequestParam Long studyId,
                                           @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<Feature> savedFeature = this.featureService.updateFeature(featureId, updatedFeature);
            return savedFeature.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by Feature ID.
     * @param featureId ID of the Feature that is to be deleted.
     * @param studyId ID of the study
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @DeleteMapping("/{featureId}")
    public ResponseEntity<?> deleteFeature(@PathVariable Long featureId,
                                           @RequestParam Long studyId,
                                           @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.featureService.deleteFeature(featureId);
            return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
