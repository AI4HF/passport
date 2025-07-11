package io.passport.server.controller;

import io.passport.server.model.*;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.DatasetService;
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
 * Class which stores the generated HTTP requests related to Dataset operations.
 */
@RestController
@RequestMapping("/dataset")
public class DatasetController {

    private static final Logger log = LoggerFactory.getLogger(DatasetController.class);

    private final String relationName = "Dataset";
    private final DatasetService datasetService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER, Role.DATA_SCIENTIST);

    @Autowired
    public DatasetController(DatasetService datasetService,
                             RoleCheckerService roleCheckerService,
                             AuditLogBookService auditLogBookService) {
        this.datasetService = datasetService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Retrieves all datasets by the given study ID.
     *
     * @param studyId   ID of the study
     * @param principal Jwt principal containing user info
     * @return List of datasets, or FORBIDDEN if user not authorized
     */
    @GetMapping
    public ResponseEntity<List<Dataset>> getAllDatasetsByStudyId(@RequestParam String studyId,
                                                                 @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Dataset> datasets = this.datasetService.getAllDatasetsByStudyId(studyId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(datasets.size()));
        return ResponseEntity.ok().headers(headers).body(datasets);
    }

    /**
     * Retrieves all datasets with FKs replaced by FK connection names..
     * @param studyId   ID of the study
     * @param principal Jwt principal containing user info
     * @return List of datasets, or FORBIDDEN if user not authorized
     */
    @GetMapping("/names")
    public ResponseEntity<List<Dataset>> getAllDatasetsWithNames(@RequestParam String studyId,
                                                                 @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Dataset> datasets = this.datasetService.getAllDatasetsWithNamesByStudyId(studyId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(datasets.size()));
        return ResponseEntity.ok().headers(headers).body(datasets);
    }

    /**
     * Retrieves a Dataset by its datasetId.
     *
     * @param datasetId ID of the Dataset
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Dataset entity or NOT FOUND
     */
    @GetMapping("/{datasetId}")
    public ResponseEntity<?> getDataset(@PathVariable String datasetId,
                                        @RequestParam String studyId,
                                        @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<Dataset> dataset = this.datasetService.findDatasetByDatasetId(datasetId);
        return dataset.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new Dataset entity.
     *
     * @param dataset   Dataset object to create
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Created Dataset or BAD_REQUEST on error
     */
    @PostMapping
    public ResponseEntity<?> createDataset(@RequestBody Dataset dataset,
                                           @RequestParam String studyId,
                                           @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            String personnelId = principal.getSubject();
            Optional<Dataset> savedDatasetOpt = this.datasetService.saveDataset(dataset, personnelId);

            if (savedDatasetOpt.isPresent()) {
                Dataset savedDataset = savedDatasetOpt.get();
                String recordId = savedDataset.getDatasetId();
                auditLogBookService.createAuditLog(
                        personnelId,
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.CREATE,
                        relationName,
                        recordId,
                        savedDataset

                );
                return ResponseEntity.ok(savedDataset);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error creating Dataset: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Updates an existing Dataset entity.
     *
     * @param datasetId      ID of the Dataset to update
     * @param updatedDataset Updated Dataset object
     * @param studyId        ID of the study for authorization
     * @param principal      Jwt principal containing user info
     * @return Updated Dataset or NOT FOUND if not present
     */
    @PutMapping("/{datasetId}")
    public ResponseEntity<?> updateDataset(@PathVariable String datasetId,
                                           @RequestBody Dataset updatedDataset,
                                           @RequestParam String studyId,
                                           @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            String personnelId = principal.getSubject();
            Optional<Dataset> savedDatasetOpt = this.datasetService.updateDataset(datasetId, updatedDataset, personnelId);

            if (savedDatasetOpt.isPresent()) {
                Dataset savedDataset = savedDatasetOpt.get();
                String recordId = savedDataset.getDatasetId();
                auditLogBookService.createAuditLog(
                        personnelId,
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.UPDATE,
                        relationName,
                        recordId,
                        savedDataset

                );
                return ResponseEntity.ok(savedDataset);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating Dataset: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Deletes an existing Dataset entity by its datasetId.
     *
     * @param datasetId ID of the Dataset to delete
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return OK on success, NOT FOUND otherwise
     */
    @DeleteMapping("/{datasetId}")
    public ResponseEntity<?> deleteDataset(@PathVariable String datasetId,
                                           @RequestParam String studyId,
                                           @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<Dataset> deletedDataset = this.datasetService.deleteDataset(datasetId);
            if (deletedDataset.isPresent()) {
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.DELETE,
                        relationName,
                        datasetId,
                        deletedDataset.get()
                );
                return ResponseEntity.status(HttpStatus.OK).body(deletedDataset.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting Dataset: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
