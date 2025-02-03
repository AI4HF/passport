package io.passport.server.controller;

import io.passport.server.model.Implementation;
import io.passport.server.model.Role;
import io.passport.server.service.AuditLogBookService; // <-- NEW
import io.passport.server.service.ImplementationService;
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
 * Class which stores the generated HTTP requests related to implementation operations.
 */
@RestController
@RequestMapping("/implementation")
public class ImplementationController {

    private static final Logger log = LoggerFactory.getLogger(ImplementationController.class);

    private final ImplementationService implementationService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService; // <-- NEW

    private final List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);

    @Autowired
    public ImplementationController(ImplementationService implementationService,
                                    RoleCheckerService roleCheckerService,
                                    AuditLogBookService auditLogBookService) {
        this.implementationService = implementationService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Read all Implementations.
     *
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return          List of Implementation objects
     */
    @GetMapping
    public ResponseEntity<List<Implementation>> getAllImplementations(@RequestParam Long studyId,
                                                                      @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Implementation> implementations = this.implementationService.getAllImplementations();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(implementations.size()));
        return ResponseEntity.ok().headers(headers).body(implementations);
    }

    /**
     * Read an Implementation by its ID.
     *
     * @param studyId         ID of the study for authorization
     * @param implementationId ID of the Implementation
     * @param principal       Jwt principal containing user info
     * @return                Implementation or NOT_FOUND
     */
    @GetMapping("/{implementationId}")
    public ResponseEntity<?> getImplementation(@RequestParam Long studyId,
                                               @PathVariable Long implementationId,
                                               @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Implementation> implOpt = this.implementationService.findImplementationById(implementationId);
        return implOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new Implementation.
     *
     * @param studyId        ID of the study for authorization
     * @param implementation Implementation model instance to create
     * @param principal      Jwt principal containing user info
     * @return               Created Implementation or BAD_REQUEST on error
     */
    @PostMapping
    public ResponseEntity<?> createImplementation(@RequestParam Long studyId,
                                                  @RequestBody Implementation implementation,
                                                  @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Implementation saved = this.implementationService.saveImplementation(implementation);
            if (saved.getImplementationId() != null) {
                String recordId = saved.getImplementationId().toString();
                String description = "Creation of Implementation " + recordId;
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        studyId,
                        "CREATE",
                        "Implementation",
                        recordId,
                        saved,
                        description
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {
            log.error("Error creating Implementation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Updates an existing Implementation by implementationId.
     *
     * @param studyId              ID of the study for authorization
     * @param implementationId     ID of the Implementation to update
     * @param updatedImplementation Updated Implementation model
     * @param principal            Jwt principal containing user info
     * @return                     Updated Implementation or NOT_FOUND
     */
    @PutMapping("/{implementationId}")
    public ResponseEntity<?> updateImplementation(@RequestParam Long studyId,
                                                  @PathVariable Long implementationId,
                                                  @RequestBody Implementation updatedImplementation,
                                                  @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<Implementation> savedOpt = this.implementationService.updateImplementation(implementationId, updatedImplementation);
            if (savedOpt.isPresent()) {
                Implementation saved = savedOpt.get();
                if (saved.getImplementationId() != null) {
                    String recordId = saved.getImplementationId().toString();
                    String description = "Update of Implementation " + recordId;
                    auditLogBookService.createAuditLog(
                            principal.getSubject(),
                            studyId,
                            "UPDATE",
                            "Implementation",
                            recordId,
                            saved,
                            description
                    );
                }
                return ResponseEntity.ok(saved);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error updating Implementation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Deletes an Implementation by implementationId.
     *
     * @param studyId          ID of the study for authorization
     * @param implementationId ID of the Implementation to delete
     * @param principal        Jwt principal containing user info
     * @return                 NO_CONTENT if deleted, NOT_FOUND otherwise
     */
    @DeleteMapping("/{implementationId}")
    public ResponseEntity<?> deleteImplementation(@RequestParam Long studyId,
                                                  @PathVariable Long implementationId,
                                                  @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.implementationService.deleteImplementation(implementationId);
            if (isDeleted) {
                String description = "Deletion of Implementation " + implementationId;
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        studyId,
                        "DELETE",
                        "Implementation",
                        implementationId.toString(),
                        null,
                        description
                );
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error deleting Implementation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
