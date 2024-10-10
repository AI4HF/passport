package io.passport.server.controller;

import io.passport.server.model.DatasetTransformation;
import io.passport.server.model.Role;
import io.passport.server.service.DatasetTransformationService;
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
 * Class which stores the generated HTTP requests related to DatasetTransformation operations.
 */
@RestController
@RequestMapping("/dataset-transformation")
public class DatasetTransformationController {
    private static final Logger log = LoggerFactory.getLogger(DatasetTransformationController.class);
    private final DatasetTransformationService datasetTransformationService;
    private final RoleCheckerService roleCheckerService;
    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);

    @Autowired
    public DatasetTransformationController(DatasetTransformationService datasetTransformationService,
                                           RoleCheckerService roleCheckerService) {
        this.datasetTransformationService = datasetTransformationService;
        this.roleCheckerService = roleCheckerService;
    }

    @GetMapping()
    public ResponseEntity<List<DatasetTransformation>> getAllDatasetTransformations(@RequestParam Long studyId,
                                                                                    @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<DatasetTransformation> datasetTransformations = this.datasetTransformationService.getAllDatasetTransformations();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(datasetTransformations.size()));

        return ResponseEntity.ok().headers(headers).body(datasetTransformations);
    }

    @GetMapping("/{dataTransformationId}")
    public ResponseEntity<?> getDatasetTransformation(@PathVariable Long dataTransformationId,
                                                      @RequestParam Long studyId,
                                                      @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<DatasetTransformation> datasetTransformation = this.datasetTransformationService
                .findDatasetTransformationByDataTransformationId(dataTransformationId);

        return datasetTransformation.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping()
    public ResponseEntity<?> createDatasetTransformation(@RequestBody DatasetTransformation datasetTransformation,
                                                         @RequestParam Long studyId,
                                                         @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            DatasetTransformation savedDatasetTransformation = this.datasetTransformationService
                    .saveDatasetTransformation(datasetTransformation);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDatasetTransformation);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{dataTransformationId}")
    public ResponseEntity<?> updateDatasetTransformation(@PathVariable Long dataTransformationId,
                                                         @RequestBody DatasetTransformation updatedDatasetTransformation,
                                                         @RequestParam Long studyId,
                                                         @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<DatasetTransformation> savedDatasetTransformation = this.datasetTransformationService
                    .updateDatasetTransformation(dataTransformationId, updatedDatasetTransformation);
            return savedDatasetTransformation.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{dataTransformationId}")
    public ResponseEntity<?> deleteDatasetTransformation(@PathVariable Long dataTransformationId,
                                                         @RequestParam Long studyId,
                                                         @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.datasetTransformationService.deleteDatasetTransformation(dataTransformationId);
            return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
