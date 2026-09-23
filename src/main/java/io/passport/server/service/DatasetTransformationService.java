package io.passport.server.service;

import io.passport.server.model.DatasetTransformation;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.DatasetTransformationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for DatasetTransformation management.
 */
@Service
public class DatasetTransformationService {

    /**
     * Dataset Transformation repo access for database management.
     */
    private final DatasetTransformationRepository datasetTransformationRepository;

    /**
     * Lazy service references for limited use in cascade validation
     */
    @Autowired @Lazy private DatasetTransformationStepService datasetTransformationStepService;
    @Autowired @Lazy private LearningDatasetService learningDatasetService;

    @Autowired
    public DatasetTransformationService(DatasetTransformationRepository datasetTransformationRepository) {
        this.datasetTransformationRepository = datasetTransformationRepository;
    }

    /**
     * Starts a validation chain of Dataset Transformations and all of their children for cascades
     *
     * @param studyId Id of the Study
     * @param datasetTransformationId Id of the Dataset Transformation
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateDatasetTransformationDeletion(String studyId, String datasetTransformationId, Jwt principal) {
        List<ValidationResult> results = new ArrayList<>();

        results.add(datasetTransformationStepService.validateCascade(studyId, "DatasetTransformation", datasetTransformationId, principal));
        results.add(learningDatasetService.validateCascade(studyId, "DatasetTransformation", datasetTransformationId, principal));

        return ValidationResult.aggregate(results);
    }

    /**
     * Return all DatasetTransformations of a study
     * @param studyId ID of the study
     * @return
     */
    public List<DatasetTransformation> getAllDatasetTransformationsByStudyId(String studyId) {
        return datasetTransformationRepository.findByStudyId(studyId);
    }

    /**
     * Find a DatasetTransformation by datasetTransformationId
     * @param datasetTransformationId ID of the DatasetTransformation
     * @return
     */
    public Optional<DatasetTransformation> findDatasetTransformationByDatasetTransformationId(String datasetTransformationId) {
        return datasetTransformationRepository.findById(datasetTransformationId);
    }

    /**
     * Save a DatasetTransformation
     * @param datasetTransformation DatasetTransformation to be saved
     * @return
     */
    public DatasetTransformation saveDatasetTransformation(DatasetTransformation datasetTransformation) {
        return datasetTransformationRepository.save(datasetTransformation);
    }

    /**
     * Update a DatasetTransformation
     * @param datasetTransformationId ID of the DatasetTransformation
     * @param updatedDatasetTransformation DatasetTransformation to be updated
     * @return
     */
    public Optional<DatasetTransformation> updateDatasetTransformation(String datasetTransformationId, DatasetTransformation updatedDatasetTransformation) {
        Optional<DatasetTransformation> oldDatasetTransformation = datasetTransformationRepository.findById(datasetTransformationId);
        if (oldDatasetTransformation.isPresent()) {
            DatasetTransformation datasetTransformation = oldDatasetTransformation.get();
            datasetTransformation.setStudyId(updatedDatasetTransformation.getStudyId());
            datasetTransformation.setTitle(updatedDatasetTransformation.getTitle());
            datasetTransformation.setDescription(updatedDatasetTransformation.getDescription());
            DatasetTransformation savedDatasetTransformation = datasetTransformationRepository.save(datasetTransformation);
            return Optional.of(savedDatasetTransformation);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a DatasetTransformation
     * @param datasetTransformationId ID of DatasetTransformation to be deleted
     * @return
     */
    public Optional<DatasetTransformation> deleteDatasetTransformation(String datasetTransformationId) {
        Optional<DatasetTransformation> existingTransformation = datasetTransformationRepository.findById(datasetTransformationId);
        if (existingTransformation.isPresent()) {
            datasetTransformationRepository.delete(existingTransformation.get());
            return existingTransformation;
        } else {
            return Optional.empty();
        }
    }
}
