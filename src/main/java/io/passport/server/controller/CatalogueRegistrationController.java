package io.passport.server.controller;

import io.passport.server.model.CatalogueRegistration;
import io.passport.server.model.Operation;
import io.passport.server.model.Role;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.RoleCheckerService;
import io.passport.server.service.CatalogueRegistrationService;
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
 * Class which stores the generated HTTP requests related to CatalogueRegistration operations.
 */
@RestController
@RequestMapping("/catalogue-registration")
public class CatalogueRegistrationController {

    private static final Logger log = LoggerFactory.getLogger(CatalogueRegistrationController.class);

    private final String relationName = "CatalogueRegistration";
    private final CatalogueRegistrationService catalogueRegistrationService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    /**
     * Roles allowed to complete the publication metadata: the study's data engineer and the
     * organization's own Data Steward.
     */
    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER, Role.DATA_STEWARD);

    @Autowired
    public CatalogueRegistrationController(CatalogueRegistrationService catalogueRegistrationService,
                              RoleCheckerService roleCheckerService,
                              AuditLogBookService auditLogBookService) {
        this.catalogueRegistrationService = catalogueRegistrationService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Reads the catalogue entries written for a publication record.
     *
     * @param catalogueDatasetId ID of the publication record
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return List of CatalogueRegistration
     */
    @GetMapping
    public ResponseEntity<List<CatalogueRegistration>> getAll(@RequestParam(required = true) String catalogueDatasetId,
                                                 @RequestParam String studyId,
                                                 @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<CatalogueRegistration> items = this.catalogueRegistrationService.findByCatalogueDatasetId(catalogueDatasetId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(items.size()));
        return ResponseEntity.ok().headers(headers).body(items);
    }

    /**
     * Reads a single CatalogueRegistration by its id.
     *
     * @param registrationId ID of the CatalogueRegistration
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return CatalogueRegistration or NOT_FOUND
     */
    @GetMapping("/{registrationId}")
    public ResponseEntity<?> getById(@PathVariable String registrationId,
                                     @RequestParam String studyId,
                                     @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<CatalogueRegistration> item = this.catalogueRegistrationService.findCatalogueRegistrationById(registrationId);
        return item.<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new CatalogueRegistration.
     *
     * @param item      The CatalogueRegistration to create
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Created CatalogueRegistration or BAD_REQUEST on error
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CatalogueRegistration item,
                                    @RequestParam String studyId,
                                    @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            CatalogueRegistration saved = this.catalogueRegistrationService.saveCatalogueRegistration(item);
            if (saved.getRegistrationId() != null) {
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.CREATE,
                        relationName,
                        saved.getRegistrationId(),
                        saved
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("Error creating CatalogueRegistration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Updates an existing CatalogueRegistration.
     *
     * @param registrationId ID of the CatalogueRegistration to update
     * @param item      Updated CatalogueRegistration data
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Updated CatalogueRegistration or NOT_FOUND
     */
    @PutMapping("/{registrationId}")
    public ResponseEntity<?> update(@PathVariable String registrationId,
                                    @RequestBody CatalogueRegistration item,
                                    @RequestParam String studyId,
                                    @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<CatalogueRegistration> savedOpt = this.catalogueRegistrationService.updateCatalogueRegistration(registrationId, item);
            if (savedOpt.isPresent()) {
                CatalogueRegistration saved = savedOpt.get();
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.UPDATE,
                        relationName,
                        saved.getRegistrationId(),
                        saved
                );
                return ResponseEntity.ok().body(saved);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating CatalogueRegistration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Deletes a CatalogueRegistration.
     *
     * @param registrationId ID of the CatalogueRegistration to delete
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return NO_CONTENT or NOT_FOUND
     */
    @DeleteMapping("/{registrationId}")
    public ResponseEntity<?> delete(@PathVariable String registrationId,
                                    @RequestParam String studyId,
                                    @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.catalogueRegistrationService.deleteCatalogueRegistration(registrationId);
            if (isDeleted) {
                auditLogBookService.createAuditLog(
                        principal,
                        studyId,
                        Operation.DELETE,
                        relationName,
                        registrationId,
                        null
                );
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting CatalogueRegistration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
