package io.passport.server.controller;

import io.passport.server.model.Implementation;
import io.passport.server.model.Role;
import io.passport.server.service.ImplementationService;
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
 * Class which stores the generated HTTP requests related to implementation operations.
 */
@RestController
@RequestMapping("/implementation")
public class ImplementationController {
    private static final Logger log = LoggerFactory.getLogger(ImplementationController.class);
    private final ImplementationService implementationService;
    private final RoleCheckerService roleCheckerService;
    private final List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);

    @Autowired
    public ImplementationController(ImplementationService implementationService, RoleCheckerService roleCheckerService) {
        this.implementationService = implementationService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read all implementations
     * @param studyId ID of the study for authorization
     * @param principal KeycloakPrincipal object that holds access token
     * @return ResponseEntity with a list of implementations
     */
    @GetMapping()
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
     * Read an implementation by id
     * @param studyId ID of the study for authorization
     * @param implementationId ID of the implementation
     * @param principal KeycloakPrincipal object that holds access token
     * @return ResponseEntity with implementation
     */
    @GetMapping("/{implementationId}")
    public ResponseEntity<?> getImplementation(@RequestParam Long studyId,
                                               @PathVariable Long implementationId,
                                               @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Implementation> implementation = this.implementationService.findImplementationById(implementationId);
        return implementation.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create Implementation.
     * @param studyId ID of the study for authorization
     * @param implementation Implementation model instance to be created
     * @param principal KeycloakPrincipal object that holds access token
     * @return ResponseEntity with created implementation
     */
    @PostMapping()
    public ResponseEntity<?> createImplementation(@RequestParam Long studyId,
                                                  @RequestBody Implementation implementation,
                                                  @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Implementation savedImplementation = this.implementationService.saveImplementation(implementation);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedImplementation);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Implementation.
     * @param studyId ID of the study for authorization
     * @param implementationId ID of the implementation that is to be updated
     * @param updatedImplementation Implementation model instance with updated details
     * @param principal KeycloakPrincipal object that holds access token
     * @return ResponseEntity with updated implementation
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

            Optional<Implementation> savedImplementation = this.implementationService.updateImplementation(implementationId, updatedImplementation);
            return savedImplementation.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by Implementation ID.
     * @param studyId ID of the study for authorization
     * @param implementationId ID of the implementation that is to be deleted
     * @param principal KeycloakPrincipal object that holds access token
     * @return ResponseEntity
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
            return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
