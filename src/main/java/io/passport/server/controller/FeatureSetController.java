package io.passport.server.controller;

import io.passport.server.model.FeatureSet;
import io.passport.server.model.Role;
import io.passport.server.service.FeatureSetService;
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
 * Class which stores the generated HTTP requests related to FeatureSet operations.
 */
@RestController
@RequestMapping("/featureset")
public class FeatureSetController {

    private static final Logger log = LoggerFactory.getLogger(FeatureSetController.class);

    private final FeatureSetService featureSetService;
    private final RoleCheckerService roleCheckerService;
    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);

    @Autowired
    public FeatureSetController(FeatureSetService featureSetService, RoleCheckerService roleCheckerService) {
        this.featureSetService = featureSetService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read all FeatureSets by studyId
     * @param studyId ID of the study
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<FeatureSet>> getAllFeatureSetsByStudyId(@RequestParam Long studyId,
                                                                       @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<FeatureSet> featureSets = this.featureSetService.getAllFeatureSetsByStudyId(studyId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(featureSets.size()));

        return ResponseEntity.ok().headers(headers).body(featureSets);
    }

    /**
     * Read a FeatureSet by id
     * @param featureSetId ID of the FeatureSet
     * @param studyId ID of the study
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping("/{featureSetId}")
    public ResponseEntity<?> getFeatureSet(@PathVariable Long featureSetId,
                                           @RequestParam Long studyId,
                                           @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<FeatureSet> featureSet = this.featureSetService.findFeatureSetByFeatureSetId(featureSetId);
        return featureSet.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create FeatureSet.
     * @param featureSet FeatureSet model instance to be created.
     * @param studyId ID of the study
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createFeatureSet(@RequestBody FeatureSet featureSet,
                                              @RequestParam Long studyId,
                                              @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            FeatureSet savedFeatureSet = this.featureSetService.saveFeatureSet(featureSet);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedFeatureSet);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update FeatureSet.
     * @param featureSetId ID of the FeatureSet that is to be updated.
     * @param updatedFeatureSet FeatureSet model instance with updated details.
     * @param studyId ID of the study
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PutMapping("/{featureSetId}")
    public ResponseEntity<?> updateFeatureSet(@PathVariable Long featureSetId,
                                              @RequestBody FeatureSet updatedFeatureSet,
                                              @RequestParam Long studyId,
                                              @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<FeatureSet> savedFeatureSet = this.featureSetService.updateFeatureSet(featureSetId, updatedFeatureSet);
            return savedFeatureSet.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by FeatureSet ID.
     * @param featureSetId ID of the FeatureSet that is to be deleted.
     * @param studyId ID of the study
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @DeleteMapping("/{featureSetId}")
    public ResponseEntity<?> deleteFeatureSet(@PathVariable Long featureSetId,
                                              @RequestParam Long studyId,
                                              @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.featureSetService.deleteFeatureSet(featureSetId);
            return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
