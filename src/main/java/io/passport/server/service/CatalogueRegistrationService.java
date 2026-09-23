package io.passport.server.service;

import io.passport.server.model.CatalogueRegistration;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.CatalogueRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service class for CatalogueRegistration management.
 */
@Service
public class CatalogueRegistrationService {

    /**
     * CatalogueRegistration repo access for database management.
     */
    private final CatalogueRegistrationRepository catalogueRegistrationRepository;
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public CatalogueRegistrationService(CatalogueRegistrationRepository catalogueRegistrationRepository, RoleCheckerService roleCheckerService) {
        this.catalogueRegistrationRepository = catalogueRegistrationRepository;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Determines which entities are to be cascaded based on the request from the previous element
     * in the chain.
     *
     * @param studyId Id of the Study
     * @param sourceResourceType Resource type of the parent element in the Cascade chain
     * @param sourceResourceId Resource id of the parent element in the Cascade chain
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateCascade(String studyId, String sourceResourceType, String sourceResourceId, Jwt principal) {
        List<CatalogueRegistration> affected;

        switch (sourceResourceType) {
            case "CatalogueDataset":
                affected = catalogueRegistrationRepository.findByCatalogueDatasetId(sourceResourceId);
                break;
            default:
                return new ValidationResult(true, "");
        }

        if (affected.isEmpty()) {
            return new ValidationResult(true, "");
        }

        boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                studyId,
                principal,
                List.of(Role.DATA_ENGINEER, Role.DATA_STEWARD)
        );

        if (!hasPermission) {
            return new ValidationResult(false, "CatalogueRegistration");
        }

        return new ValidationResult(true, "CatalogueRegistration");
    }

    /**
     * Return the catalogue entries written for a publication record
     * @param catalogueDatasetId ID of the CatalogueDataset
     * @return
     */
    public List<CatalogueRegistration> findByCatalogueDatasetId(String catalogueDatasetId) {
        return catalogueRegistrationRepository.findByCatalogueDatasetId(catalogueDatasetId);
    }

    /**
     * Return the entry a publication record has in one catalogue, if any
     * @param catalogueDatasetId ID of the CatalogueDataset
     * @param catalogueType The catalogue
     * @return
     */
    public List<CatalogueRegistration> findByCatalogueDatasetIdAndCatalogueType(String catalogueDatasetId, String catalogueType) {
        return catalogueRegistrationRepository.findByCatalogueDatasetIdAndCatalogueType(catalogueDatasetId, catalogueType);
    }

    /**
     * Find a CatalogueRegistration by registrationId
     * @param registrationId ID of the CatalogueRegistration
     * @return
     */
    public Optional<CatalogueRegistration> findCatalogueRegistrationById(String registrationId) {
        return catalogueRegistrationRepository.findById(registrationId);
    }

    /**
     * Save a CatalogueRegistration
     * @param catalogueRegistration CatalogueRegistration to be saved
     * @return
     */
    public CatalogueRegistration saveCatalogueRegistration(CatalogueRegistration catalogueRegistration) {
        catalogueRegistration.setCreatedAt(Instant.now());
        catalogueRegistration.setLastUpdatedAt(Instant.now());
        return catalogueRegistrationRepository.save(catalogueRegistration);
    }

    /**
     * Update a CatalogueRegistration
     * @param registrationId ID of the CatalogueRegistration
     * @param updated CatalogueRegistration to be updated
     * @return
     */
    public Optional<CatalogueRegistration> updateCatalogueRegistration(String registrationId, CatalogueRegistration updated) {
        Optional<CatalogueRegistration> existing = catalogueRegistrationRepository.findById(registrationId);
        if (existing.isPresent()) {
            CatalogueRegistration catalogueRegistration = existing.get();
            catalogueRegistration.setCatalogueDatasetId(updated.getCatalogueDatasetId());
            catalogueRegistration.setCatalogueType(updated.getCatalogueType());
            catalogueRegistration.setCatalogueUri(updated.getCatalogueUri());
            catalogueRegistration.setListingDate(updated.getListingDate());
            catalogueRegistration.setChangeType(updated.getChangeType());
            catalogueRegistration.setLastUpdatedBy(updated.getLastUpdatedBy());
            catalogueRegistration.setLastUpdatedAt(Instant.now());
            return Optional.of(catalogueRegistrationRepository.save(catalogueRegistration));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a CatalogueRegistration
     * @param registrationId ID of the CatalogueRegistration to be deleted
     * @return
     */
    public boolean deleteCatalogueRegistration(String registrationId) {
        if (catalogueRegistrationRepository.existsById(registrationId)) {
            catalogueRegistrationRepository.deleteById(registrationId);
            return true;
        } else {
            return false;
        }
    }
}
