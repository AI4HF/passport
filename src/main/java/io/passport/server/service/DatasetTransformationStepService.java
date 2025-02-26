package io.passport.server.service;

import io.passport.server.model.DatasetTransformationStep;
import io.passport.server.repository.DatasetTransformationStepRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public DatasetTransformationStepService(DatasetTransformationStepRepository datasetTransformationStepRepository) {
        this.datasetTransformationStepRepository = datasetTransformationStepRepository;
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
}
