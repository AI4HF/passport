package io.passport.server.controller;

import io.passport.server.model.Organization;
import io.passport.server.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Class which stores the generated HTTP requests related to organization operations.
 */
@RestController
@RequestMapping("/organization")
public class OrganizationController {
    /**
     * Organization repo access for database management.
     */
    private final OrganizationRepository organizationRepository;

    @Autowired
    public OrganizationController(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    /**
     * Read all Organizations.
     * @return
     */
    @GetMapping("/")
    public ResponseEntity<List<Organization>> getAllOrganizations() {
        List<Organization> organizations = organizationRepository.findAll();
        return ResponseEntity.ok(organizations);
    }

    /**
     * Create Organization.
     * @param organization Organization model instance to be created.
     * @return
     */
    @PostMapping("/")
    public ResponseEntity<Organization> createOrganization(@RequestBody Organization organization) {
        Organization savedOrganization = organizationRepository.save(organization);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrganization);
    }

    /**
     * Delete by Organization ID.
     * @param organizationId ID of the organization that is to be deleted.
     * @return
     */
    @DeleteMapping("/{organizationId}")
    public ResponseEntity<Object> deleteOrganization(@PathVariable Long organizationId) {
        return organizationRepository.findById(organizationId)
                .map(organization -> {
                    organizationRepository.delete(organization);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}