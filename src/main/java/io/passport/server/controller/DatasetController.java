package io.passport.server.controller;

import io.passport.server.model.Dataset;
import io.passport.server.model.Role;
import io.passport.server.service.DatasetService;
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
 * Class which stores the generated HTTP requests related to Dataset operations.
 */
@RestController
@RequestMapping("/dataset")
public class DatasetController {
    private static final Logger log = LoggerFactory.getLogger(DatasetController.class);
    private final DatasetService datasetService;
    private final RoleCheckerService roleCheckerService;
    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER, Role.DATA_SCIENTIST);

    @Autowired
    public DatasetController(DatasetService datasetService, RoleCheckerService roleCheckerService) {
        this.datasetService = datasetService;
        this.roleCheckerService = roleCheckerService;
    }

    @GetMapping()
    public ResponseEntity<List<Dataset>> getAllDatasetsByStudyId(@RequestParam Long studyId,
                                                                 @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Dataset> datasets = this.datasetService.getAllDatasetsByStudyId(studyId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(datasets.size()));

        return ResponseEntity.ok().headers(headers).body(datasets);
    }

    @GetMapping("/{datasetId}")
    public ResponseEntity<?> getDataset(@PathVariable Long datasetId,
                                        @RequestParam Long studyId,
                                        @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Dataset> dataset = this.datasetService.findDatasetByDatasetId(datasetId);
        return dataset.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping()
    public ResponseEntity<?> createDataset(@RequestBody Dataset dataset,
                                           @RequestParam Long studyId,
                                           @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            String personnelId = principal.getSubject();
            Optional<Dataset> savedDataset = this.datasetService.saveDataset(dataset, personnelId);
            return savedDataset.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{datasetId}")
    public ResponseEntity<?> updateDataset(@PathVariable Long datasetId,
                                           @RequestBody Dataset updatedDataset,
                                           @RequestParam Long studyId,
                                           @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            String personnelId = principal.getSubject();
            Optional<Dataset> savedDataset = this.datasetService.updateDataset(datasetId, updatedDataset, personnelId);
            return savedDataset.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{datasetId}")
    public ResponseEntity<?> deleteDataset(@PathVariable Long datasetId,
                                           @RequestParam Long studyId,
                                           @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.datasetService.deleteDataset(datasetId);
            return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
