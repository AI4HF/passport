package io.passport.server.service;

import io.passport.server.model.DatasetTransformationStep;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.DatasetTransformationStepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service class for DatasetTransformationStep management.
 */
@Service
public class DatasetTransformationStepService {

    /**
     * DatasetTransformationStep repo access for database management.
     */
    private final DatasetTransformationStepRepository datasetTransformationStepRepository;
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public DatasetTransformationStepService(DatasetTransformationStepRepository datasetTransformationStepRepository,
                                            RoleCheckerService roleCheckerService) {
        this.datasetTransformationStepRepository = datasetTransformationStepRepository;
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
        List<DatasetTransformationStep> affectedSteps;

        switch (sourceResourceType) {
            case "DatasetTransformation":
                affectedSteps = datasetTransformationStepRepository.findByDataTransformationId(sourceResourceId);
                break;
            default:
                return new ValidationResult(1, "");
        }

        if (affectedSteps.isEmpty()) {
            return new ValidationResult(1, "");
        }

        boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                studyId,
                principal,
                List.of(Role.DATA_ENGINEER, Role.DATA_SCIENTIST)
        );

        if (!hasPermission) {
            return new ValidationResult(0, "DatasetTransformationStep");
        }

        return new ValidationResult(1, "DatasetTransformationStep");
    }

    /**
     * Return all DatasetTransformationSteps
     * @return
     */
    public List<DatasetTransformationStep> getAllDatasetTransformationSteps() {
        return datasetTransformationStepRepository.findAll();
    }

    /**
     * Find DatasetTransformationSteps by dataTransformationId
     * @param dataTransformationId ID of the DatasetTransformation
     * @return
     */
    public List<DatasetTransformationStep> findByDataTransformationId(String dataTransformationId) {
        return datasetTransformationStepRepository.findByDataTransformationId(dataTransformationId);
    }

    /**
     * Find a DatasetTransformationStep by stepId
     * @param stepId ID of the DatasetTransformationStep
     * @return
     */
    public Optional<DatasetTransformationStep> findDatasetTransformationStepByStepId(String stepId) {
        return datasetTransformationStepRepository.findById(stepId);
    }

    /**
     * Save a DatasetTransformationStep
     * @param datasetTransformationStep DatasetTransformationStep to be saved
     * @return
     */
    public DatasetTransformationStep saveDatasetTransformationStep(DatasetTransformationStep datasetTransformationStep) {
        datasetTransformationStep.setCreatedAt(Instant.now());
        datasetTransformationStep.setLastUpdatedAt(Instant.now());
        return datasetTransformationStepRepository.save(datasetTransformationStep);
    }

    /**
     * Update a DatasetTransformationStep
     * @param stepId ID of the DatasetTransformationStep
     * @param updatedDatasetTransformationStep DatasetTransformationStep to be updated
     * @return
     */
    public Optional<DatasetTransformationStep> updateDatasetTransformationStep(String stepId, DatasetTransformationStep updatedDatasetTransformationStep) {
        Optional<DatasetTransformationStep> oldDatasetTransformationStep = datasetTransformationStepRepository.findById(stepId);
        if (oldDatasetTransformationStep.isPresent()) {
            DatasetTransformationStep datasetTransformationStep = oldDatasetTransformationStep.get();
            datasetTransformationStep.setDataTransformationId(updatedDatasetTransformationStep.getDataTransformationId());
            datasetTransformationStep.setInputFeatures(updatedDatasetTransformationStep.getInputFeatures());
            datasetTransformationStep.setOutputFeatures(updatedDatasetTransformationStep.getOutputFeatures());
            datasetTransformationStep.setMethod(updatedDatasetTransformationStep.getMethod());
            datasetTransformationStep.setExplanation(updatedDatasetTransformationStep.getExplanation());
            datasetTransformationStep.setCreatedBy(updatedDatasetTransformationStep.getCreatedBy());
            datasetTransformationStep.setLastUpdatedAt(Instant.now());
            datasetTransformationStep.setLastUpdatedBy(updatedDatasetTransformationStep.getLastUpdatedBy());
            DatasetTransformationStep savedDatasetTransformationStep = datasetTransformationStepRepository.save(datasetTransformationStep);
            return Optional.of(savedDatasetTransformationStep);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a DatasetTransformationStep
     * @param stepId ID of DatasetTransformationStep to be deleted
     * @return
     */
    public Optional<DatasetTransformationStep> deleteDatasetTransformationStep(String stepId) {
        Optional<DatasetTransformationStep> existingStep = datasetTransformationStepRepository.findById(stepId);
        if (existingStep.isPresent()) {
            datasetTransformationStepRepository.delete(existingStep.get());
            return existingStep;
        } else {
            return Optional.empty();
        }
    }

    /**
     * Find Steps created or last updated by a specific personnel
     *
     * @param personnelId Id of the Personnel
     */
    public List<DatasetTransformationStep> findByCreatedByOrLastUpdatedBy(String personnelId) {
        return datasetTransformationStepRepository.findByCreatedByOrLastUpdatedBy(personnelId);
    }

    /**
     * Resolve the Study ID for a given Step ID directly via a repository call
     *
     * @param stepId Id of the Transformation Step
     */
    public Optional<String> findStudyIdByStepId(String stepId) {
        return datasetTransformationStepRepository.findStudyIdByStepId(stepId);
    }
}
