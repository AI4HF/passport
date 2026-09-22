package io.passport.server.controller;

import io.passport.server.model.DatasetDistribution;
import io.passport.server.model.Operation;
import io.passport.server.model.Role;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.RoleCheckerService;
import io.passport.server.service.DatasetDistributionService;
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
 * Class which stores the generated HTTP requests related to DatasetDistribution operations.
 */
@RestController
@RequestMapping("/dataset-distribution")
public class DatasetDistributionController {

    private static final Logger log = LoggerFactory.getLogger(DatasetDistributionController.class);

    private final String relationName = "DatasetDistribution";
    private final DatasetDistributionService datasetDistributionService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    /**
     * Roles allowed to complete the publication metadata: the study's data engineer and the
     * organization's own Data Steward.
     */
    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER, Role.DATA_STEWARD);

    @Autowired
    public DatasetDistributionController(DatasetDistributionService datasetDistributionService,
                              RoleCheckerService roleCheckerService,
                              AuditLogBookService auditLogBookService) {
        this.datasetDistributionService = datasetDistributionService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Reads the distributions of a publication record.
     *
     * @param catalogueDatasetId ID of the publication record
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return List of DatasetDistribution
     */
    @GetMapping
    public ResponseEntity<List<DatasetDistribution>> getAll(@RequestParam(required = true) String catalogueDatasetId,
                                                 @RequestParam String studyId,
                                                 @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<DatasetDistribution> items = this.datasetDistributionService.findByCatalogueDatasetId(catalogueDatasetId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(items.size()));
        return ResponseEntity.ok().headers(headers).body(items);
    }

    /**
     * Reads a single DatasetDistribution by its id.
     *
     * @param distributionId ID of the DatasetDistribution
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return DatasetDistribution or NOT_FOUND
     */
    @GetMapping("/{distributionId}")
    public ResponseEntity<?> getById(@PathVariable String distributionId,
                                     @RequestParam String studyId,
                                     @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<DatasetDistribution> item = this.datasetDistributionService.findDatasetDistributionById(distributionId);
        return item.<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new DatasetDistribution.
     *
     * @param item      The DatasetDistribution to create
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Created DatasetDistribution or BAD_REQUEST on error
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody DatasetDistribution item,
                                    @RequestParam String studyId,
                                    @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            DatasetDistribution saved = this.datasetDistributionService.saveDatasetDistribution(item);
            if (saved.getDistributionId() != null) {
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.CREATE,
                        relationName,
                        saved.getDistributionId(),
                        saved
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("Error creating DatasetDistribution: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Updates an existing DatasetDistribution.
     *
     * @param distributionId ID of the DatasetDistribution to update
     * @param item      Updated DatasetDistribution data
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Updated DatasetDistribution or NOT_FOUND
     */
    @PutMapping("/{distributionId}")
    public ResponseEntity<?> update(@PathVariable String distributionId,
                                    @RequestBody DatasetDistribution item,
                                    @RequestParam String studyId,
                                    @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<DatasetDistribution> savedOpt = this.datasetDistributionService.updateDatasetDistribution(distributionId, item);
            if (savedOpt.isPresent()) {
                DatasetDistribution saved = savedOpt.get();
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.UPDATE,
                        relationName,
                        saved.getDistributionId(),
                        saved
                );
                return ResponseEntity.ok().body(saved);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating DatasetDistribution: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Deletes a DatasetDistribution.
     *
     * @param distributionId ID of the DatasetDistribution to delete
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return NO_CONTENT or NOT_FOUND
     */
    @DeleteMapping("/{distributionId}")
    public ResponseEntity<?> delete(@PathVariable String distributionId,
                                    @RequestParam String studyId,
                                    @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.datasetDistributionService.deleteDatasetDistribution(distributionId);
            if (isDeleted) {
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.DELETE,
                        relationName,
                        distributionId,
                        null
                );
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting DatasetDistribution: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
