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
    /**
     * Implementation service for implementation management
     */
    private final ImplementationService implementationService;

    /**
     * Role checker service for authorization
     */
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public ImplementationController(ImplementationService implementationService, RoleCheckerService roleCheckerService) {
        this.implementationService = implementationService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read all implementations
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<Implementation>> getAllImplementations(@AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Implementation> implementations = this.implementationService.getAllImplementations();

        long totalCount = implementations.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(implementations);
    }

    /**
     * Read an implementation by id
     * @param implementationId ID of the implementation
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping("/{implementationId}")
    public ResponseEntity<?> getImplementation(@PathVariable Long implementationId,
                                               @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Implementation> implementation = this.implementationService.findImplementationById(implementationId);

        if(implementation.isPresent()) {
            return ResponseEntity.ok().body(implementation.get());
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create Implementation.
     * @param implementation Implementation model instance to be created.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createImplementation(@RequestBody Implementation implementation,
                                                  @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Implementation savedImplementation = this.implementationService.saveImplementation(implementation);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedImplementation);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Implementation.
     * @param implementationId ID of the implementation that is to be updated.
     * @param updatedImplementation Implementation model instance with updated details.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PutMapping("/{implementationId}")
    public ResponseEntity<?> updateImplementation(@PathVariable Long implementationId,
                                                  @RequestBody Implementation updatedImplementation,
                                                  @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<Implementation> savedImplementation = this.implementationService.updateImplementation(implementationId, updatedImplementation);
            if(savedImplementation.isPresent()) {
                return ResponseEntity.ok().body(savedImplementation);
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by Implementation ID.
     * @param implementationId ID of the implementation that is to be deleted.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @DeleteMapping("/{implementationId}")
    public ResponseEntity<?> deleteImplementation(@PathVariable Long implementationId,
                                                  @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.implementationService.deleteImplementation(implementationId);
            if(isDeleted) {
                return ResponseEntity.noContent().build();
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
