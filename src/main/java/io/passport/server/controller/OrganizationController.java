package io.passport.server.controller;

import io.passport.server.model.Organization;
import io.passport.server.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    /**
     * Organization repo access for database management.
     */
    private final OrganizationRepository organizationRepository;

    @Autowired
    public OrganizationController(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @GetMapping("/{page}")
    public ResponseEntity<List<Organization>> getAllOrganizations(@PathVariable int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Organization> organizationPage = organizationRepository.findAll(pageable);

        List<Organization> organizations = organizationPage.getContent();

        long totalCount = organizationRepository.count();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(organizations);
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
     * Update Organization.
     * @param id ID of the organization that is to be updated.
     * @param updatedOrganization model instance with updated details.
     * @return
     */
    @PutMapping("/{id}")
    public ResponseEntity<Organization> updateOrganization(@PathVariable Long id, @RequestBody Organization updatedOrganization) {
        Optional<Organization> optionalOrganization = organizationRepository.findById(id);
        if (optionalOrganization.isPresent()) {
            Organization organization = optionalOrganization.get();
            organization.setName(updatedOrganization.getName());
            organization.setAddress(updatedOrganization.getAddress());
            Organization savedOrganization = organizationRepository.save(organization);
            return ResponseEntity.ok(savedOrganization);
        } else {
            return ResponseEntity.notFound().build();
        }
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