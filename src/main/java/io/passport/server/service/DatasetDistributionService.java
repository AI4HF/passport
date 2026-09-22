package io.passport.server.service;

import io.passport.server.model.DatasetDistribution;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.DatasetDistributionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service class for DatasetDistribution management.
 */
@Service
public class DatasetDistributionService {

    /**
     * DatasetDistribution repo access for database management.
     */
    private final DatasetDistributionRepository datasetDistributionRepository;
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public DatasetDistributionService(DatasetDistributionRepository datasetDistributionRepository, RoleCheckerService roleCheckerService) {
        this.datasetDistributionRepository = datasetDistributionRepository;
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
        List<DatasetDistribution> affected;

        switch (sourceResourceType) {
            case "CatalogueDataset":
                affected = datasetDistributionRepository.findByCatalogueDatasetId(sourceResourceId);
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
            return new ValidationResult(false, "DatasetDistribution");
        }

        return new ValidationResult(true, "DatasetDistribution");
    }

    /**
     * Return the distributions of a publication record
     * @param catalogueDatasetId ID of the CatalogueDataset
     * @return
     */
    public List<DatasetDistribution> findByCatalogueDatasetId(String catalogueDatasetId) {
        return datasetDistributionRepository.findByCatalogueDatasetId(catalogueDatasetId);
    }

    /**
     * Find a DatasetDistribution by distributionId
     * @param distributionId ID of the DatasetDistribution
     * @return
     */
    public Optional<DatasetDistribution> findDatasetDistributionById(String distributionId) {
        return datasetDistributionRepository.findById(distributionId);
    }

    /**
     * Save a DatasetDistribution
     * @param datasetDistribution DatasetDistribution to be saved
     * @return
     */
    public DatasetDistribution saveDatasetDistribution(DatasetDistribution datasetDistribution) {
        datasetDistribution.setCreatedAt(Instant.now());
        datasetDistribution.setLastUpdatedAt(Instant.now());
        return datasetDistributionRepository.save(datasetDistribution);
    }

    /**
     * Update a DatasetDistribution
     * @param distributionId ID of the DatasetDistribution
     * @param updated DatasetDistribution to be updated
     * @return
     */
    public Optional<DatasetDistribution> updateDatasetDistribution(String distributionId, DatasetDistribution updated) {
        Optional<DatasetDistribution> existing = datasetDistributionRepository.findById(distributionId);
        if (existing.isPresent()) {
            DatasetDistribution datasetDistribution = existing.get();
            datasetDistribution.setCatalogueDatasetId(updated.getCatalogueDatasetId());
            datasetDistribution.setTitle(updated.getTitle());
            datasetDistribution.setDescription(updated.getDescription());
            datasetDistribution.setAccessUrl(updated.getAccessUrl());
            datasetDistribution.setDownloadUrl(updated.getDownloadUrl());
            datasetDistribution.setAccessServiceUri(updated.getAccessServiceUri());
            datasetDistribution.setFormat(updated.getFormat());
            datasetDistribution.setMediaType(updated.getMediaType());
            datasetDistribution.setAvailability(updated.getAvailability());
            datasetDistribution.setStatus(updated.getStatus());
            datasetDistribution.setLicence(updated.getLicence());
            datasetDistribution.setRights(updated.getRights());
            datasetDistribution.setByteSize(updated.getByteSize());
            datasetDistribution.setChecksumAlgorithm(updated.getChecksumAlgorithm());
            datasetDistribution.setChecksumValue(updated.getChecksumValue());
            datasetDistribution.setLastUpdatedBy(updated.getLastUpdatedBy());
            datasetDistribution.setLastUpdatedAt(Instant.now());
            return Optional.of(datasetDistributionRepository.save(datasetDistribution));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a DatasetDistribution
     * @param distributionId ID of the DatasetDistribution to be deleted
     * @return
     */
    public boolean deleteDatasetDistribution(String distributionId) {
        if (datasetDistributionRepository.existsById(distributionId)) {
            datasetDistributionRepository.deleteById(distributionId);
            return true;
        } else {
            return false;
        }
    }
}
