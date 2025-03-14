package io.passport.server.controller;

import io.passport.server.model.Organization;
import io.passport.server.model.Role;
import io.passport.server.service.OrganizationService;
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
 * Class which stores the generated HTTP requests related to organization operations.
 */
@RestController
@RequestMapping("/organization")
public class OrganizationController {

    private static final Logger log = LoggerFactory.getLogger(OrganizationController.class);

    /**
     * Organization service for organization management.
     */
    private final OrganizationService organizationService;

    /**
     * Role checker service for authorization
     */
    private final RoleCheckerService roleCheckerService;

    /**
     * List of authorized roles for this endpoint
     */
    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER, Role.DATA_SCIENTIST, Role.ML_ENGINEER,
            Role.ORGANIZATION_ADMIN, Role.QUALITY_ASSURANCE_SPECIALIST, Role.STUDY_OWNER, Role.SURVEY_MANAGER);

    @Autowired
    public OrganizationController(OrganizationService organizationService, RoleCheckerService roleCheckerService) {
        this.organizationService = organizationService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read all organizations or filter by organization admin id
     * @param organizationAdminId ID of the organization admin
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<Organization>> getAllOrganizations(@RequestParam(required = false) String organizationAdminId,
                                                                  @AuthenticationPrincipal Jwt principal) {

        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Organization> organizations;

        if(organizationAdminId != null) {
            organizations = this.organizationService.findOrganizationByOrganizationAdminId(organizationAdminId);
        }else{
            organizations = this.organizationService.getAllOrganizations();
        }

        long totalCount = organizations.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(organizations);
    }

    /**
     * Read an organization by id
     * @param organizationId ID of the organization
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping("/{organizationId}")
    public ResponseEntity<?> getOrganizationById(@PathVariable String organizationId, @AuthenticationPrincipal Jwt principal) {

        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Organization> organization = this.organizationService.findOrganizationById(organizationId);

        if (organization.isPresent()) {
            return ResponseEntity.ok().body(organization);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


        /**
         * Create an Organization.
         * @param organization Organization model instance to be created.
         * @param principal KeycloakPrincipal object that holds access token
         * @return
         */
    @PostMapping()
    public ResponseEntity<?> createOrganization(@RequestBody Organization organization, @AuthenticationPrincipal Jwt principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> lesserAllowedRoles = List.of(Role.ORGANIZATION_ADMIN);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, lesserAllowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Organization savedOrganization = this.organizationService.saveOrganization(organization);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedOrganization);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Organization.
     * @param organizationId ID of the organization that is to be updated.
     * @param updatedOrganization model instance with updated details.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PutMapping("/{organizationId}")
    public ResponseEntity<?> updateOrganization(@PathVariable String organizationId,
                                                @RequestBody Organization updatedOrganization,
                                                @AuthenticationPrincipal Jwt principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> lesserAllowedRoles = List.of(Role.ORGANIZATION_ADMIN);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, lesserAllowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<Organization> savedOrganization = this.organizationService.updateOrganization(organizationId, updatedOrganization);
            if (savedOrganization.isPresent()) {
                return ResponseEntity.ok().body(savedOrganization.get());
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete an organization by Organization ID.
     * @param organizationId ID of the organization that is to be deleted.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @DeleteMapping("/{organizationId}")
    public ResponseEntity<?> deleteOrganization(@PathVariable String organizationId,
                                                @AuthenticationPrincipal Jwt principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> lesserAllowedRoles = List.of(Role.ORGANIZATION_ADMIN);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, lesserAllowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.organizationService.deleteOrganization(organizationId);
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