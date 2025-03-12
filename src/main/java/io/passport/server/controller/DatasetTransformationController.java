package io.passport.server.controller;

import io.passport.server.model.*;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.DatasetTransformationService;
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
 * Class which stores the generated HTTP requests related to DatasetTransformation operations.
 */
@RestController
@RequestMapping("/dataset-transformation")
public class DatasetTransformationController {

    private static final Logger log = LoggerFactory.getLogger(DatasetTransformationController.class);

    private final String relationName = "Dataset Transformation";
    private final DatasetTransformationService datasetTransformationService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);

    @Autowired
    public DatasetTransformationController(DatasetTransformationService datasetTransformationService,
                                           RoleCheckerService roleCheckerService,
                                           AuditLogBookService auditLogBookService) {
        this.datasetTransformationService = datasetTransformationService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Retrieves all DatasetTransformations (for authorized DATA_ENGINEER).
     *
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return List of all DatasetTransformations
     */
    @GetMapping
    public ResponseEntity<List<DatasetTransformation>> getAllDatasetTransformations(@RequestParam String studyId,
                                                                                    @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<DatasetTransformation> datasetTransformations = this.datasetTransformationService.getAllDatasetTransformations();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(datasetTransformations.size()));
        return ResponseEntity.ok().headers(headers).body(datasetTransformations);
    }

    /**
     * Retrieves a single DatasetTransformation by its ID.
     *
     * @param dataTransformationId ID of the DatasetTransformation
     * @param studyId              ID of the study for authorization
     * @param principal            Jwt principal containing user info
     * @return DatasetTransformation or NOT FOUND
     */
    @GetMapping("/{dataTransformationId}")
    public ResponseEntity<?> getDatasetTransformation(@PathVariable String dataTransformationId,
                                                      @RequestParam String studyId,
                                                      @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<DatasetTransformation> dtOpt = this.datasetTransformationService
                .findDatasetTransformationByDataTransformationId(dataTransformationId);
        return dtOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new DatasetTransformation.
     *
     * @param datasetTransformation The DatasetTransformation to be created
     * @param studyId               ID of the study for authorization
     * @param principal             Jwt principal containing user info
     * @return The created DatasetTransformation
     */
    @PostMapping
    public ResponseEntity<?> createDatasetTransformation(@RequestBody DatasetTransformation datasetTransformation,
                                                         @RequestParam String studyId,
                                                         @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            DatasetTransformation saved = this.datasetTransformationService.saveDatasetTransformation(datasetTransformation);

            if (saved.getDataTransformationId() != null) {
                String recordId = saved.getDataTransformationId();
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
            log.error("Error creating DatasetTransformation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Updates an existing DatasetTransformation by its ID.
     *
     * @param dataTransformationId         ID of the DatasetTransformation to update
     * @param updatedDatasetTransformation Updated details
     * @param studyId                      ID of the study for authorization
     * @param principal                    Jwt principal containing user info
     * @return The updated DatasetTransformation or NOT FOUND
     */
    @PutMapping("/{dataTransformationId}")
    public ResponseEntity<?> updateDatasetTransformation(@PathVariable String dataTransformationId,
                                                         @RequestBody DatasetTransformation updatedDatasetTransformation,
                                                         @RequestParam String studyId,
                                                         @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<DatasetTransformation> savedOpt =
                    this.datasetTransformationService.updateDatasetTransformation(dataTransformationId, updatedDatasetTransformation);

            if (savedOpt.isPresent()) {
                DatasetTransformation saved = savedOpt.get();
                String recordId = saved.getDataTransformationId();
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
            log.error("Error updating DatasetTransformation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Deletes a DatasetTransformation by its ID.
     *
     * @param dataTransformationId ID of the DatasetTransformation to delete
     * @param studyId              ID of the study for authorization
     * @param principal            Jwt principal containing user info
     * @return OK if deleted, NOT_FOUND otherwise
     */
    @DeleteMapping("/{dataTransformationId}")
    public ResponseEntity<?> deleteDatasetTransformation(@PathVariable String dataTransformationId,
                                                         @RequestParam String studyId,
                                                         @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<DatasetTransformation> deletedDatasetTransformation = this.datasetTransformationService.deleteDatasetTransformation(dataTransformationId);
            if (deletedDatasetTransformation.isPresent()) {
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.DELETE,
                        relationName,
                        dataTransformationId,
                        deletedDatasetTransformation.get()
                );
                return ResponseEntity.status(HttpStatus.OK).body(deletedDatasetTransformation.get());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error deleting DatasetTransformation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
