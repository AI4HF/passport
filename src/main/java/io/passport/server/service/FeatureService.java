package io.passport.server.service;

import io.passport.server.model.Feature;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.FeatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Feature management.
 */
@Service
public class FeatureService {

    /**
     * Feature repo access for database management.
     */
    private final FeatureRepository featureRepository;
    private final RoleCheckerService roleCheckerService;

    /**
     * Lazy service references for limited use in cascade validation
     */
    @Autowired @Lazy private FeatureDatasetCharacteristicService featureDatasetCharacteristicService;

    @Autowired
    public FeatureService(FeatureRepository featureRepository,
                          RoleCheckerService roleCheckerService) {
        this.featureRepository = featureRepository;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Starts a validation chain of Feature and all of their children for cascades
     *
     * @param studyId Id of the Study
     * @param featureId Id of Feature
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateFeatureDeletion(String studyId, String featureId, Jwt principal) {
        List<ValidationResult> results = new ArrayList<>();

        results.add(featureDatasetCharacteristicService.validateCascade(studyId, "Feature", featureId, principal));

        return ValidationResult.aggregate(results);
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
        List<Feature> affectedFeatures;

        switch (sourceResourceType) {
            case "FeatureSet":
                affectedFeatures = featureRepository.findByFeaturesetId(sourceResourceId);
                break;
            default:
                return new ValidationResult(1, "");
        }

        if (affectedFeatures.isEmpty()) {
            return new ValidationResult(1, "");
        }

        boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                studyId,
                principal,
                List.of(Role.DATA_ENGINEER)
        );

        if (!hasPermission) {
            return new ValidationResult(0, "Feature");
        }

        return new ValidationResult(1, "Feature");
    }
    /**
     * Return all Features
     * @return
     */
    public List<Feature> getAllFeatures() {
        return featureRepository.findAll();
    }

    /**
     * Find Features by featuresetId
     * @param featuresetId ID of the FeatureSet
     * @return
     */
    public List<Feature> findByFeaturesetId(String featuresetId) {
        return featureRepository.findByFeaturesetId(featuresetId);
    }

    /**
     * Find a Feature by featureId
     * @param featureId ID of the Feature
     * @return
     */
    public Optional<Feature> findFeatureByFeatureId(String featureId) {
        return featureRepository.findById(featureId);
    }

    /**
     * Save a Feature
     * @param feature Feature to be saved
     * @return
     */
    public Feature saveFeature(Feature feature) {
        feature.setCreatedAt(Instant.now());
        feature.setLastUpdatedAt(Instant.now());
        return featureRepository.save(feature);
    }

    /**
     * Update a Feature
     * @param featureId ID of the Feature
     * @param updatedFeature Feature to be updated
     * @return
     */
    public Optional<Feature> updateFeature(String featureId, Feature updatedFeature) {
        Optional<Feature> oldFeature = featureRepository.findById(featureId);
        if (oldFeature.isPresent()) {
            Feature feature = oldFeature.get();
            feature.setFeaturesetId(updatedFeature.getFeaturesetId());
            feature.setTitle(updatedFeature.getTitle());
            feature.setDescription(updatedFeature.getDescription());
            feature.setDataType(updatedFeature.getDataType());
            feature.setIsOutcome(updatedFeature.getIsOutcome());
            feature.setMandatory(updatedFeature.getMandatory());
            feature.setIsUnique(updatedFeature.getIsUnique());
            feature.setUnits(updatedFeature.getUnits());
            feature.setEquipment(updatedFeature.getEquipment());
            feature.setDataCollection(updatedFeature.getDataCollection());
            feature.setLastUpdatedAt(Instant.now());
            feature.setLastUpdatedBy(updatedFeature.getLastUpdatedBy());
            Feature savedFeature = featureRepository.save(feature);
            return Optional.of(savedFeature);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a Feature
     * @param featureId ID of Feature to be deleted
     * @return
     */
    public Optional<Feature> deleteFeature(String featureId) {
        Optional<Feature> existingFeature = featureRepository.findById(featureId);
        if (existingFeature.isPresent()) {
            featureRepository.delete(existingFeature.get());
            return existingFeature;
        } else {
            return Optional.empty();
        }
    }

    /**
     * Find Features created or last updated by a specific personnel.
     */
    public List<Feature> findByCreatedByOrLastUpdatedBy(String personnelId) {
        return featureRepository.findByCreatedByOrLastUpdatedBy(personnelId);
    }

    /**
     * Resolve the Study ID for a given Feature ID directly via DB query.
     */
    public Optional<String> findStudyIdByFeatureId(String featureId) {
        return featureRepository.findStudyIdByFeatureId(featureId);
    }

}
