package io.passport.server.controller;

import io.passport.server.model.Organization;
import io.passport.server.service.OrganizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    /**
     * Read all organizations
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<Organization>> getAllOrganizations() {

        List<Organization> organizations = this.organizationService.getAllOrganizations();

        long totalCount = organizations.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(organizations);
    }

    /**
     * Read an organization by id
     * @param organizationId ID of the organization
     * @return
     */
    @GetMapping("/{organizationId}")
    public ResponseEntity<?> getOrganizationById(@PathVariable Long organizationId) {
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
         * @return
         */
    @PostMapping()
    public ResponseEntity<?> createOrganization(@RequestBody Organization organization) {
        try{
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
     * @return
     */
    @PutMapping("/{organizationId}")
    public ResponseEntity<?> updateOrganization(@PathVariable Long organizationId, @RequestBody Organization updatedOrganization) {
        try{
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
     * @return
     */
    @DeleteMapping("/{organizationId}")
    public ResponseEntity<?> deleteOrganization(@PathVariable Long organizationId) {
        try{
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