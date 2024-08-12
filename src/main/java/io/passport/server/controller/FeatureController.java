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

    /**
     * Feature service for Feature management
     */
    private final FeatureService featureService;

    /**
     * Role checker service for authorization
     */
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public FeatureController(FeatureService featureService, RoleCheckerService roleCheckerService) {
        this.featureService = featureService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read all Features or filtered by featuresetId
     * @param featuresetId ID of the FeatureSet (optional)
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<Feature>> getFeatures(
            @RequestParam(required = false) Long featuresetId,
            @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);
        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Feature> features;

        if (featuresetId != null) {
            features = this.featureService.findByFeaturesetId(featuresetId);
        } else {
            features = this.featureService.getAllFeatures();
        }

        long totalCount = features.size();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(features);
    }


    /**
     * Read a Feature by id
     * @param featureId ID of the Feature
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping("/{featureId}")
    public ResponseEntity<?> getFeature(@PathVariable Long featureId,
                                        @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);
        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Feature> feature = this.featureService.findFeatureByFeatureId(featureId);

        if(feature.isPresent()) {
            return ResponseEntity.ok().body(feature.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create Feature.
     * @param feature Feature model instance to be created.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createFeature(@RequestBody Feature feature,
                                           @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Feature savedFeature = this.featureService.saveFeature(feature);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedFeature);
        } catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Feature.
     * @param featureId ID of the Feature that is to be updated.
     * @param updatedFeature Feature model instance with updated details.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PutMapping("/{featureId}")
    public ResponseEntity<?> updateFeature(@PathVariable Long featureId,
                                           @RequestBody Feature updatedFeature,
                                           @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<Feature> savedFeature = this.featureService.updateFeature(featureId, updatedFeature);
            if(savedFeature.isPresent()) {
                return ResponseEntity.ok().body(savedFeature);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by Feature ID.
     * @param featureId ID of the Feature that is to be deleted.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @DeleteMapping("/{featureId}")
    public ResponseEntity<?> deleteFeature(@PathVariable Long featureId,
                                           @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.featureService.deleteFeature(featureId);
            if(isDeleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
