package io.passport.server.service;

import io.passport.server.model.FeatureDatasetCharacteristic;
import io.passport.server.model.FeatureDatasetCharacteristicId;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.FeatureDatasetCharacteristicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for FeatureDatasetCharacteristic management.
 */
@Service
public class FeatureDatasetCharacteristicService {

    /**
     * FeatureDatasetCharacteristic repo access for database management.
     */
    private final FeatureDatasetCharacteristicRepository featureDatasetCharacteristicRepository;
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public FeatureDatasetCharacteristicService(FeatureDatasetCharacteristicRepository featureDatasetCharacteristicRepository,
                                               RoleCheckerService roleCheckerService) {
        this.featureDatasetCharacteristicRepository = featureDatasetCharacteristicRepository;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Determines which entities are to be cascaded based on the request from the previous element in the chain
     * Continues the chain by directing to the next entries through the other validation method
     *
     * @param studyId Id of the Study
     * @param sourceResourceType Resource type of the parent element in the Cascade chain
     * @param sourceResourceId Resource id of the parent element in the Cascade chain
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateCascade(String studyId, String sourceResourceType, String sourceResourceId, Jwt principal) {
        List<FeatureDatasetCharacteristic> affectedCharacteristics;

        switch (sourceResourceType) {
            case "Dataset":
                affectedCharacteristics = featureDatasetCharacteristicRepository.findByIdDatasetId(sourceResourceId);
                break;
            case "Feature":
                affectedCharacteristics = featureDatasetCharacteristicRepository.findByIdFeatureId(sourceResourceId);
                break;
            default:
                return new ValidationResult(true, "");
        }

        if (affectedCharacteristics.isEmpty()) {
            return new ValidationResult(true, "");
        }

        boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                studyId,
                principal,
                List.of(Role.DATA_ENGINEER)
        );

        if (!hasPermission) {
            return new ValidationResult(false, "FeatureDatasetCharacteristic");
        }

        return new ValidationResult(true, "FeatureDatasetCharacteristic");
    }

    /**
     * Return all FeatureDatasetCharacteristics
     * @return
     */
    public List<FeatureDatasetCharacteristic> getAllFeatureDatasetCharacteristics() {
        return featureDatasetCharacteristicRepository.findAll();
    }

    /**
     * Find FeatureDatasetCharacteristics by datasetId
     * @param datasetId ID of the Dataset
     * @return
     */
    public List<FeatureDatasetCharacteristic> findByDatasetId(String datasetId) {
        return featureDatasetCharacteristicRepository.findByIdDatasetId(datasetId);
    }

    /**
     * Find FeatureDatasetCharacteristics by featureId
     * @param featureId ID of the Feature
     * @return
     */
    public List<FeatureDatasetCharacteristic> findByFeatureId(String featureId) {
        return featureDatasetCharacteristicRepository.findByIdFeatureId(featureId);
    }

    /**
     * Find a FeatureDatasetCharacteristic by composite id
     * @param featureId ID of the Feature
     * @param datasetId ID of the Dataset
     * @return
     */
    public List<FeatureDatasetCharacteristic> findByFeatureIdAndDatasetId(String featureId, String datasetId) {
        return featureDatasetCharacteristicRepository.findByIdFeatureIdAndIdDatasetId(featureId, datasetId);
    }

    /**
     * Save a FeatureDatasetCharacteristic
     * @param featureDatasetCharacteristic FeatureDatasetCharacteristic to be saved
     * @return
     */
    public FeatureDatasetCharacteristic saveFeatureDatasetCharacteristic(FeatureDatasetCharacteristic featureDatasetCharacteristic) {
        return featureDatasetCharacteristicRepository.save(featureDatasetCharacteristic);
    }

    /**
     * Update a FeatureDatasetCharacteristic
     * @param featureDatasetCharacteristicId composite ID of the FeatureDatasetCharacteristic
     * @param updatedFeatureDatasetCharacteristic FeatureDatasetCharacteristic to be updated
     * @return
     */
    public Optional<FeatureDatasetCharacteristic> updateFeatureDatasetCharacteristic(FeatureDatasetCharacteristicId featureDatasetCharacteristicId, FeatureDatasetCharacteristic updatedFeatureDatasetCharacteristic) {
        Optional<FeatureDatasetCharacteristic> oldFeatureDatasetCharacteristic = featureDatasetCharacteristicRepository.findById(featureDatasetCharacteristicId);
        if (oldFeatureDatasetCharacteristic.isPresent()) {
            FeatureDatasetCharacteristic featureDatasetCharacteristic = oldFeatureDatasetCharacteristic.get();
            featureDatasetCharacteristic.setValue(updatedFeatureDatasetCharacteristic.getValue());
            featureDatasetCharacteristic.setValueDataType(updatedFeatureDatasetCharacteristic.getValueDataType());
            FeatureDatasetCharacteristic savedFeatureDatasetCharacteristic = featureDatasetCharacteristicRepository.save(featureDatasetCharacteristic);
            return Optional.of(savedFeatureDatasetCharacteristic);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a FeatureDatasetCharacteristic
     * @param featureDatasetCharacteristicId composite ID of FeatureDatasetCharacteristic to be deleted
     * @return
     */
    public Optional<FeatureDatasetCharacteristic> deleteFeatureDatasetCharacteristic(FeatureDatasetCharacteristicId featureDatasetCharacteristicId) {
        Optional<FeatureDatasetCharacteristic> existingCharacteristic = featureDatasetCharacteristicRepository.findById(featureDatasetCharacteristicId);
        if (existingCharacteristic.isPresent()) {
            featureDatasetCharacteristicRepository.delete(existingCharacteristic.get());
            return existingCharacteristic;
        } else {
            return Optional.empty();
        }
    }

}
