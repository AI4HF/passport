package io.passport.server.service;

import io.passport.server.model.FeatureSet;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.FeatureSetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for FeatureSet management.
 */
@Service
public class FeatureSetService {

    /**
     * FeatureSet repo access for database management.
     */
    private final FeatureSetRepository featureSetRepository;
    private final RoleCheckerService roleCheckerService;

    /**
     * Lazy service references for limited use in cascade validation
     */
    @Autowired @Lazy private FeatureService featureService;
    @Autowired @Lazy private DatasetService datasetService;

    @Autowired
    public FeatureSetService(FeatureSetRepository featureSetRepository,
                             RoleCheckerService roleCheckerService) {
        this.featureSetRepository = featureSetRepository;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Starts a validation chain of Feature Set and all of their children for cascades
     *
     * @param studyId Id of the Study
     * @param featuresetId Id of the Feature Set
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateFeatureSetDeletion(String studyId, String featuresetId, Jwt principal) {
        List<ValidationResult> results = new ArrayList<>();

        results.add(featureService.validateCascade(studyId, "FeatureSet", featuresetId, principal));
        results.add(datasetService.validateCascade(studyId, "FeatureSet", featuresetId, principal));

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
        List<FeatureSet> affectedFeatureSets;

        switch (sourceResourceType) {
            case "Experiment":
                affectedFeatureSets = featureSetRepository.findByExperimentId(sourceResourceId);
                break;
            default:
                return new ValidationResult(true, "");
        }

        if (affectedFeatureSets.isEmpty()) {
            return new ValidationResult(true, "");
        }

        List<ValidationResult> childResults = new ArrayList<>();
        boolean authorized = true;

        for (FeatureSet fs : affectedFeatureSets) {
            boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                    studyId,
                    principal,
                    List.of(Role.DATA_ENGINEER)
            );

            if (!hasPermission) {
                authorized = false;
                break;
            }

            childResults.add(validateFeatureSetDeletion(studyId, fs.getFeaturesetId(), principal));
        }

        if (!authorized) {
            return new ValidationResult(false, "FeatureSet");
        }

        childResults.add(new ValidationResult(true, "FeatureSet"));

        return ValidationResult.aggregate(childResults);
    }

    /**
     * Return all FeatureSets
     * @return
     */
    public List<FeatureSet> getAllFeatureSets() {
        return featureSetRepository.findAll();
    }

    /**
     * Return all FeatureSets by studyId
     * @param studyId ID of the study
     * @return
     */
    public List<FeatureSet> getAllFeatureSetsByStudyId(String studyId) {
        return featureSetRepository.findFeatureSetByStudyId(studyId);
    }

    /**
     * Find a FeatureSet by featureSetId
     * @param featureSetId ID of the FeatureSet
     * @return
     */
    public Optional<FeatureSet> findFeatureSetByFeatureSetId(String featureSetId) {
        return featureSetRepository.findById(featureSetId);
    }

    /**
     * Save a FeatureSet
     * @param featureSet FeatureSet to be saved
     * @return
     */
    public FeatureSet saveFeatureSet(FeatureSet featureSet) {
        featureSet.setCreatedAt(Instant.now());
        featureSet.setLastUpdatedAt(Instant.now());
        return featureSetRepository.save(featureSet);
    }

    /**
     * Update a FeatureSet
     * @param featureSetId ID of the FeatureSet
     * @param updatedFeatureSet FeatureSet to be updated
     * @return
     */
    public Optional<FeatureSet> updateFeatureSet(String featureSetId, FeatureSet updatedFeatureSet) {
        Optional<FeatureSet> oldFeatureSet = featureSetRepository.findById(featureSetId);
        if (oldFeatureSet.isPresent()) {
            FeatureSet featureSet = oldFeatureSet.get();
            featureSet.setTitle(updatedFeatureSet.getTitle());
            featureSet.setFeaturesetURL(updatedFeatureSet.getFeaturesetURL());
            featureSet.setDescription(updatedFeatureSet.getDescription());
            featureSet.setLastUpdatedAt(Instant.now());
            featureSet.setLastUpdatedBy(updatedFeatureSet.getLastUpdatedBy());
            FeatureSet savedFeatureSet = featureSetRepository.save(featureSet);
            return Optional.of(savedFeatureSet);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a FeatureSet
     * @param featureSetId ID of FeatureSet to be deleted
     * @return
     */
    public Optional<FeatureSet> deleteFeatureSet(String featureSetId) {
        Optional<FeatureSet> existingFeatureSet = featureSetRepository.findById(featureSetId);
        if (existingFeatureSet.isPresent()) {
            featureSetRepository.delete(existingFeatureSet.get());
            return existingFeatureSet;
        } else {
            return Optional.empty();
        }
    }

    /**
     * Efficiently find the Study ID associated with a FeatureSet.
     */
    public String findStudyIdByFeatureSetId(String featuresetId) {
        return featureSetRepository.findStudyIdByFeatureSetId(featuresetId);
    }

    /**
     * Find FeatureSets created or last updated by a specific personnel.
     */
    public List<FeatureSet> findByCreatedByOrLastUpdatedBy(String personnelId) {
        return featureSetRepository.findByCreatedByOrLastUpdatedBy(personnelId);
    }

}
