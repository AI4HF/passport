package io.passport.server.service;

import io.passport.server.model.CatalogueDataset;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.CatalogueDatasetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service class for CatalogueDataset management.
 */
@Service
public class CatalogueDatasetService {

    /**
     * CatalogueDataset repo access for database management.
     */
    private final CatalogueDatasetRepository catalogueDatasetRepository;
    private final RoleCheckerService roleCheckerService;

    /**
     * Lazy service references for limited use in cascade validation
     */
    @Autowired @Lazy private DatasetDistributionService datasetDistributionService;
    @Autowired @Lazy private CatalogueRegistrationService catalogueRegistrationService;

    @Autowired
    public CatalogueDatasetService(CatalogueDatasetRepository catalogueDatasetRepository, RoleCheckerService roleCheckerService) {
        this.catalogueDatasetRepository = catalogueDatasetRepository;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Starts a validation chain of CatalogueDataset and all of their children for cascades
     *
     * @param studyId Id of the Study
     * @param catalogueDatasetId Id of the CatalogueDataset
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateCatalogueDatasetDeletion(String studyId, String catalogueDatasetId, Jwt principal) {
        List<ValidationResult> results = new java.util.ArrayList<>();

        results.add(datasetDistributionService.validateCascade(studyId, "CatalogueDataset", catalogueDatasetId, principal));
        results.add(catalogueRegistrationService.validateCascade(studyId, "CatalogueDataset", catalogueDatasetId, principal));

        return ValidationResult.aggregate(results);
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
        List<CatalogueDataset> affected;

        switch (sourceResourceType) {
            case "Dataset":
                affected = catalogueDatasetRepository.findByDatasetId(sourceResourceId).stream().toList();
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
            return new ValidationResult(false, "CatalogueDataset");
        }

        List<ValidationResult> childResults = new java.util.ArrayList<>();
        for (CatalogueDataset cd : affected) {
            childResults.add(validateCatalogueDatasetDeletion(studyId, cd.getCatalogueDatasetId(), principal));
        }
        childResults.add(new ValidationResult(true, "CatalogueDataset"));
        return ValidationResult.aggregate(childResults);
    }

    /**
     * Return the publication record of a dataset, if it has one
     * @param datasetId ID of the dataset
     * @return
     */
    public Optional<CatalogueDataset> findByDatasetId(String datasetId) {
        return catalogueDatasetRepository.findByDatasetId(datasetId);
    }

    /**
     * Return the publication records of a study
     * @param studyId ID of the study
     * @return
     */
    public List<CatalogueDataset> findByStudyId(String studyId) {
        return catalogueDatasetRepository.findCatalogueDatasetsByStudyId(studyId);
    }

    /**
     * Find a CatalogueDataset by catalogueDatasetId
     * @param catalogueDatasetId ID of the CatalogueDataset
     * @return
     */
    public Optional<CatalogueDataset> findCatalogueDatasetById(String catalogueDatasetId) {
        return catalogueDatasetRepository.findById(catalogueDatasetId);
    }

    /**
     * Save a CatalogueDataset
     * @param catalogueDataset CatalogueDataset to be saved
     * @return
     */
    public CatalogueDataset saveCatalogueDataset(CatalogueDataset catalogueDataset) {
        catalogueDataset.setCreatedAt(Instant.now());
        catalogueDataset.setLastUpdatedAt(Instant.now());
        return catalogueDatasetRepository.save(catalogueDataset);
    }

    /**
     * Update a CatalogueDataset
     * @param catalogueDatasetId ID of the CatalogueDataset
     * @param updated CatalogueDataset to be updated
     * @return
     */
    public Optional<CatalogueDataset> updateCatalogueDataset(String catalogueDatasetId, CatalogueDataset updated) {
        Optional<CatalogueDataset> existing = catalogueDatasetRepository.findById(catalogueDatasetId);
        if (existing.isPresent()) {
            CatalogueDataset catalogueDataset = existing.get();
            catalogueDataset.setDatasetId(updated.getDatasetId());
            catalogueDataset.setPublicTitle(updated.getPublicTitle());
            catalogueDataset.setPublicDescription(updated.getPublicDescription());
            catalogueDataset.setAccessRights(updated.getAccessRights());
            catalogueDataset.setHdabName(updated.getHdabName());
            catalogueDataset.setHdabUri(updated.getHdabUri());
            catalogueDataset.setPublisherName(updated.getPublisherName());
            catalogueDataset.setPublisherType(updated.getPublisherType());
            catalogueDataset.setContactPoint(updated.getContactPoint());
            catalogueDataset.setLegalBasis(updated.getLegalBasis());
            catalogueDataset.setPurpose(updated.getPurpose());
            catalogueDataset.setPersonalData(updated.getPersonalData());
            catalogueDataset.setPublicationApprovalReference(updated.getPublicationApprovalReference());
            catalogueDataset.setApplicableLegislation(updated.getApplicableLegislation());
            catalogueDataset.setRetentionPeriodStart(updated.getRetentionPeriodStart());
            catalogueDataset.setRetentionPeriodEnd(updated.getRetentionPeriodEnd());
            catalogueDataset.setLandingPage(updated.getLandingPage());
            catalogueDataset.setDocumentation(updated.getDocumentation());
            catalogueDataset.setQualityAnnotation(updated.getQualityAnnotation());
            catalogueDataset.setLastUpdatedBy(updated.getLastUpdatedBy());
            catalogueDataset.setLastUpdatedAt(Instant.now());
            return Optional.of(catalogueDatasetRepository.save(catalogueDataset));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a CatalogueDataset
     * @param catalogueDatasetId ID of the CatalogueDataset to be deleted
     * @return
     */
    public boolean deleteCatalogueDataset(String catalogueDatasetId) {
        if (catalogueDatasetRepository.existsById(catalogueDatasetId)) {
            catalogueDatasetRepository.deleteById(catalogueDatasetId);
            return true;
        } else {
            return false;
        }
    }
}
