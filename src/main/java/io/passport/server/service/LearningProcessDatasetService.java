package io.passport.server.service;

import io.passport.server.model.LearningProcessDataset;
import io.passport.server.model.LearningProcessDatasetId;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.LearningProcessDatasetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for LearningProcessDataset management.
 */
@Service
public class LearningProcessDatasetService {

    /**
     * LearningProcessDataset repo access for database management.
     */
    private final LearningProcessDatasetRepository learningProcessDatasetRepository;
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public LearningProcessDatasetService(LearningProcessDatasetRepository learningProcessDatasetRepository,
                                         RoleCheckerService roleCheckerService) {
        this.learningProcessDatasetRepository = learningProcessDatasetRepository;
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
        List<LearningProcessDataset> affectedRecords;

        switch (sourceResourceType) {
            case "LearningDataset":
                affectedRecords = learningProcessDatasetRepository.findByIdLearningDatasetId(sourceResourceId);
                break;
            case "LearningProcess":
                affectedRecords = learningProcessDatasetRepository.findByIdLearningProcessId(sourceResourceId);
                break;
            default:
                return new ValidationResult(1, "");
        }

        if (affectedRecords.isEmpty()) {
            return new ValidationResult(1, "");
        }

        boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                studyId,
                principal,
                List.of(Role.DATA_SCIENTIST)
        );

        if (!hasPermission) {
            return new ValidationResult(0, "LearningProcessDataset");
        }

        return new ValidationResult(1, "LearningProcessDataset");
    }

    /**
     * Return all LearningProcessDatasets
     * @return
     */
    public List<LearningProcessDataset> getAllLearningProcessDatasets() {
        return learningProcessDatasetRepository.findAll();
    }

    /**
     * Find LearningProcessDatasets by learningProcessId
     * @param learningProcessId ID of the LearningProcess
     * @return
     */
    public List<LearningProcessDataset> findByLearningProcessId(String learningProcessId) {
        return learningProcessDatasetRepository.findByIdLearningProcessId(learningProcessId);
    }

    /**
     * Find LearningProcessDatasets by learningDatasetId
     * @param learningDatasetId ID of the LearningDataset
     * @return
     */
    public List<LearningProcessDataset> findByLearningDatasetId(String learningDatasetId) {
        return learningProcessDatasetRepository.findByIdLearningDatasetId(learningDatasetId);
    }

    /**
     * Find a LearningProcessDataset by composite id
     * @param learningProcessDatasetId composite ID of the LearningProcessDataset
     * @return
     */
    public Optional<LearningProcessDataset> findLearningProcessDatasetById(LearningProcessDatasetId learningProcessDatasetId) {
        return learningProcessDatasetRepository.findById(learningProcessDatasetId);
    }

    /**
     * Save a LearningProcessDataset
     * @param learningProcessDataset LearningProcessDataset to be saved
     * @return
     */
    public LearningProcessDataset saveLearningProcessDataset(LearningProcessDataset learningProcessDataset) {
        return learningProcessDatasetRepository.save(learningProcessDataset);
    }

    /**
     * Update a LearningProcessDataset
     * @param learningProcessDatasetId composite ID of the LearningProcessDataset
     * @param updatedLearningProcessDataset LearningProcessDataset to be updated
     * @return
     */
    public Optional<LearningProcessDataset> updateLearningProcessDataset(LearningProcessDatasetId learningProcessDatasetId, LearningProcessDataset updatedLearningProcessDataset) {
        Optional<LearningProcessDataset> oldLearningProcessDataset = learningProcessDatasetRepository.findById(learningProcessDatasetId);
        if (oldLearningProcessDataset.isPresent()) {
            LearningProcessDataset learningProcessDataset = oldLearningProcessDataset.get();
            learningProcessDataset.setDescription(updatedLearningProcessDataset.getDescription());
            LearningProcessDataset savedLearningProcessDataset = learningProcessDatasetRepository.save(learningProcessDataset);
            return Optional.of(savedLearningProcessDataset);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a LearningProcessDataset
     * @param learningProcessDatasetId composite ID of LearningProcessDataset to be deleted
     * @return
     */
    public Optional<LearningProcessDataset> deleteLearningProcessDataset(LearningProcessDatasetId learningProcessDatasetId) {
        Optional<LearningProcessDataset> existingProcessDataset = learningProcessDatasetRepository.findById(learningProcessDatasetId);
        if (existingProcessDataset.isPresent()) {
            learningProcessDatasetRepository.delete(existingProcessDataset.get());
            return existingProcessDataset;
        } else {
            return Optional.empty();
        }
    }

}
