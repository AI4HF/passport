package io.passport.server.service;

import io.passport.server.model.Organization;
import io.passport.server.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for organization management.
 */
@Service
public class OrganizationService {

    /**
     * Organization repo access for database management.
     */
    private final OrganizationRepository organizationRepository;

    @Autowired
    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    /**
     * Get all organizations
     */
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    /**
     * Find an organization by organizationId
     * @param organizationId ID of the organization
     * @return
     */
    public Optional<Organization> findOrganizationById(Long organizationId) {
        return organizationRepository.findById(organizationId);
    }

    /**
     * Find an organization by organizationAdminId
     * @param organizationAdminId ID of the organization admin
     * @return
     */
    public List<Organization> findOrganizationByOrganizationAdminId(String organizationAdminId) {
        return organizationRepository.findByOrganizationAdminId(organizationAdminId);
    }

    /**
     * Save an organization
     * @param organization organization to be saved
     * @return
     */
    public Organization saveOrganization(Organization organization) {
        return organizationRepository.save(organization);
    }

    /**
     * Update an organization
     * @param organizationId ID of the organization
     * @param updatedOrganization organization to be updated
     * @return
     */
    public Optional<Organization> updateOrganization(Long organizationId, Organization updatedOrganization) {
        Optional<Organization> oldOrganization = organizationRepository.findById(organizationId);
        if (oldOrganization.isPresent()) {
            Organization organization = oldOrganization.get();
            organization.setName(updatedOrganization.getName());
            organization.setAddress(updatedOrganization.getAddress());
            Organization savedOrganization = organizationRepository.save(organization);
            return Optional.of(savedOrganization);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete an organization
     * @param organizationId ID of organization to be deleted
     * @return
     */
    public boolean deleteOrganization(Long organizationId) {
        if(organizationRepository.existsById(organizationId)) {
            organizationRepository.deleteById(organizationId);
            return true;
        }else{
            return false;
        }
    }
}
