package io.passport.server.controller;

import io.passport.server.model.CatalogueDataset;
import io.passport.server.model.Operation;
import io.passport.server.model.Role;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.OrganizationScopeService;
import io.passport.server.service.RoleCheckerService;
import io.passport.server.service.CatalogueDatasetService;
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
 * Class which stores the generated HTTP requests related to CatalogueDataset operations.
 *
 * Writes are organization-scoped for the Data Steward: see OrganizationScopeService.
 */
@RestController
@RequestMapping("/catalogue-dataset")
public class CatalogueDatasetController {

    private static final Logger log = LoggerFactory.getLogger(CatalogueDatasetController.class);

    private final String relationName = "CatalogueDataset";
    private final CatalogueDatasetService catalogueDatasetService;
    private final RoleCheckerService roleCheckerService;
    private final OrganizationScopeService organizationScopeService;
    private final AuditLogBookService auditLogBookService;

    /**
     * Roles allowed to complete the publication metadata: the study's data engineer and the
     * organization's own Data Steward.
     */
    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER, Role.DATA_STEWARD);

    @Autowired
    public CatalogueDatasetController(CatalogueDatasetService catalogueDatasetService,
                              RoleCheckerService roleCheckerService,
                              OrganizationScopeService organizationScopeService,
                              AuditLogBookService auditLogBookService) {
        this.catalogueDatasetService = catalogueDatasetService;
        this.roleCheckerService = roleCheckerService;
        this.organizationScopeService = organizationScopeService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Reads the publication records of a study, optionally for one dataset.
     *
     * @param datasetId Optional dataset filter
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return List of CatalogueDataset
     */
    @GetMapping
    public ResponseEntity<List<CatalogueDataset>> getAll(@RequestParam(required = false) String datasetId,
                                                 @RequestParam String studyId,
                                                 @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<CatalogueDataset> items = datasetId != null
                ? this.catalogueDatasetService.findByDatasetId(datasetId).stream().toList()
                : this.catalogueDatasetService.findByStudyId(studyId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(items.size()));
        return ResponseEntity.ok().headers(headers).body(items);
    }

    /**
     * Reads a single CatalogueDataset by its id.
     *
     * @param catalogueDatasetId ID of the CatalogueDataset
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return CatalogueDataset or NOT_FOUND
     */
    @GetMapping("/{catalogueDatasetId}")
    public ResponseEntity<?> getById(@PathVariable String catalogueDatasetId,
                                     @RequestParam String studyId,
                                     @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<CatalogueDataset> item = this.catalogueDatasetService.findCatalogueDatasetById(catalogueDatasetId);
        return item.<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new CatalogueDataset.
     *
     * @param item      The CatalogueDataset to create
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Created CatalogueDataset or BAD_REQUEST on error
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CatalogueDataset item,
                                    @RequestParam String studyId,
                                    @AuthenticationPrincipal Jwt principal) {
        try {
            // A Data Steward may only write the metadata of their own organization's dataset
            if (!this.organizationScopeService.isCallerAuthorizedForDatasetMetadata(
                    studyId, item.getDatasetId(), principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            CatalogueDataset saved = this.catalogueDatasetService.saveCatalogueDataset(item);
            if (saved.getCatalogueDatasetId() != null) {
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.CREATE,
                        relationName,
                        saved.getCatalogueDatasetId(),
                        saved
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("Error creating CatalogueDataset: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Updates an existing CatalogueDataset.
     *
     * @param catalogueDatasetId ID of the CatalogueDataset to update
     * @param item      Updated CatalogueDataset data
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Updated CatalogueDataset or NOT_FOUND
     */
    @PutMapping("/{catalogueDatasetId}")
    public ResponseEntity<?> update(@PathVariable String catalogueDatasetId,
                                    @RequestBody CatalogueDataset item,
                                    @RequestParam String studyId,
                                    @AuthenticationPrincipal Jwt principal) {
        try {
            // A Data Steward may only write the metadata of their own organization's dataset
            if (!this.organizationScopeService.isCallerAuthorizedForDatasetMetadata(
                    studyId, item.getDatasetId(), principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<CatalogueDataset> savedOpt = this.catalogueDatasetService.updateCatalogueDataset(catalogueDatasetId, item);
            if (savedOpt.isPresent()) {
                CatalogueDataset saved = savedOpt.get();
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.UPDATE,
                        relationName,
                        saved.getCatalogueDatasetId(),
                        saved
                );
                return ResponseEntity.ok().body(saved);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating CatalogueDataset: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Deletes a CatalogueDataset.
     *
     * @param catalogueDatasetId ID of the CatalogueDataset to delete
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return NO_CONTENT or NOT_FOUND
     */
    @DeleteMapping("/{catalogueDatasetId}")
    public ResponseEntity<?> delete(@PathVariable String catalogueDatasetId,
                                    @RequestParam String studyId,
                                    @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.catalogueDatasetService.deleteCatalogueDataset(catalogueDatasetId);
            if (isDeleted) {
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.DELETE,
                        relationName,
                        catalogueDatasetId,
                        null
                );
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting CatalogueDataset: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
