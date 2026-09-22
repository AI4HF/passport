package io.passport.server.controller;

import io.passport.server.model.DatasetConcept;
import io.passport.server.model.Operation;
import io.passport.server.model.Role;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.OrganizationScopeService;
import io.passport.server.service.RoleCheckerService;
import io.passport.server.service.DatasetConceptService;
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
 * Class which stores the generated HTTP requests related to DatasetConcept operations.
 *
 * Writes are organization-scoped for the Data Steward: see OrganizationScopeService.
 */
@RestController
@RequestMapping("/dataset-concept")
public class DatasetConceptController {

    private static final Logger log = LoggerFactory.getLogger(DatasetConceptController.class);

    private final String relationName = "DatasetConcept";
    private final DatasetConceptService datasetConceptService;
    private final RoleCheckerService roleCheckerService;
    private final OrganizationScopeService organizationScopeService;
    private final AuditLogBookService auditLogBookService;

    /**
     * Roles allowed to complete the publication metadata: the study's data engineer and the
     * organization's own Data Steward.
     */
    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER, Role.DATA_STEWARD);

    @Autowired
    public DatasetConceptController(DatasetConceptService datasetConceptService,
                              RoleCheckerService roleCheckerService,
                              OrganizationScopeService organizationScopeService,
                              AuditLogBookService auditLogBookService) {
        this.datasetConceptService = datasetConceptService;
        this.roleCheckerService = roleCheckerService;
        this.organizationScopeService = organizationScopeService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Reads the controlled-vocabulary values of a dataset.
     *
     * @param datasetId ID of the dataset
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return List of DatasetConcept
     */
    @GetMapping
    public ResponseEntity<List<DatasetConcept>> getAll(@RequestParam(required = true) String datasetId,
                                                 @RequestParam String studyId,
                                                 @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<DatasetConcept> items = this.datasetConceptService.findByDatasetId(datasetId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(items.size()));
        return ResponseEntity.ok().headers(headers).body(items);
    }

    /**
     * Reads a single DatasetConcept by its id.
     *
     * @param conceptId ID of the DatasetConcept
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return DatasetConcept or NOT_FOUND
     */
    @GetMapping("/{conceptId}")
    public ResponseEntity<?> getById(@PathVariable String conceptId,
                                     @RequestParam String studyId,
                                     @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<DatasetConcept> item = this.datasetConceptService.findDatasetConceptById(conceptId);
        return item.<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new DatasetConcept.
     *
     * @param item      The DatasetConcept to create
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Created DatasetConcept or BAD_REQUEST on error
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody DatasetConcept item,
                                    @RequestParam String studyId,
                                    @AuthenticationPrincipal Jwt principal) {
        try {
            // A Data Steward may only write the metadata of their own organization's dataset
            if (!this.organizationScopeService.isCallerAuthorizedForDatasetMetadata(
                    studyId, item.getDatasetId(), principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            DatasetConcept saved = this.datasetConceptService.saveDatasetConcept(item);
            if (saved.getConceptId() != null) {
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.CREATE,
                        relationName,
                        saved.getConceptId(),
                        saved
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("Error creating DatasetConcept: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Updates an existing DatasetConcept.
     *
     * @param conceptId ID of the DatasetConcept to update
     * @param item      Updated DatasetConcept data
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Updated DatasetConcept or NOT_FOUND
     */
    @PutMapping("/{conceptId}")
    public ResponseEntity<?> update(@PathVariable String conceptId,
                                    @RequestBody DatasetConcept item,
                                    @RequestParam String studyId,
                                    @AuthenticationPrincipal Jwt principal) {
        try {
            // A Data Steward may only write the metadata of their own organization's dataset
            if (!this.organizationScopeService.isCallerAuthorizedForDatasetMetadata(
                    studyId, item.getDatasetId(), principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<DatasetConcept> savedOpt = this.datasetConceptService.updateDatasetConcept(conceptId, item);
            if (savedOpt.isPresent()) {
                DatasetConcept saved = savedOpt.get();
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.UPDATE,
                        relationName,
                        saved.getConceptId(),
                        saved
                );
                return ResponseEntity.ok().body(saved);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating DatasetConcept: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Deletes a DatasetConcept.
     *
     * @param conceptId ID of the DatasetConcept to delete
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return NO_CONTENT or NOT_FOUND
     */
    @DeleteMapping("/{conceptId}")
    public ResponseEntity<?> delete(@PathVariable String conceptId,
                                    @RequestParam String studyId,
                                    @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.datasetConceptService.deleteDatasetConcept(conceptId);
            if (isDeleted) {
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.DELETE,
                        relationName,
                        conceptId,
                        null
                );
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting DatasetConcept: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
