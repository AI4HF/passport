package io.passport.server.service;

import io.passport.server.model.DatasetTransformation;
import io.passport.server.model.LearningDataset;
import io.passport.server.model.LearningDatasetandTransformationDTO;
import io.passport.server.repository.DatasetTransformationRepository;
import io.passport.server.repository.LearningDatasetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Service class for LearningDataset management.
 */
@Service
public class LearningDatasetService {

    /**
     * LearningDataset and DatasetTransformation repo access for database management.
     */
    private final LearningDatasetRepository learningDatasetRepository;
    private final DatasetTransformationRepository datasetTransformationRepository;

    private final StudyService studyService;

    @Autowired
    public LearningDatasetService(LearningDatasetRepository learningDatasetRepository, DatasetTransformationRepository datasetTransformationRepository, StudyService studyService) {
        this.learningDatasetRepository = learningDatasetRepository;
        this.datasetTransformationRepository = datasetTransformationRepository;
        this.studyService = studyService;
    }

    /**
     * Return all LearningDatasets by studyId
     * @param studyId ID of the study
     * @return
     */
    public List<LearningDataset> getAllLearningDatasetsByStudyId(Long studyId) {
        return learningDatasetRepository.findAllByStudyId(studyId);
    }

    /**
     * Find LearningDatasets by dataTransformationId
     * @param dataTransformationId ID of the DataTransformation
     * @return
     */
    public List<LearningDataset> findByDataTransformationId(Long dataTransformationId) {
        return learningDatasetRepository.findByDataTransformationId(dataTransformationId);
    }

    /**
     * Find LearningDatasets by datasetId
     * @param datasetId ID of the Dataset
     * @return
     */
    public List<LearningDataset> findByDatasetId(Long datasetId) {
        return learningDatasetRepository.findByDatasetId(datasetId);
    }

    /**
     * Find a LearningDataset by learningDatasetId
     * @param learningDatasetId ID of the LearningDataset
     * @return
     */
    public Optional<LearningDataset> findLearningDatasetByLearningDatasetId(Long learningDatasetId) {
        return learningDatasetRepository.findById(learningDatasetId);
    }

    /**
     * Delete a LearningDataset
     * @param learningDatasetId ID of LearningDataset to be deleted
     * @return
     */
    public Optional<LearningDataset> deleteLearningDataset(Long learningDatasetId) {
        Optional<LearningDataset> existingLearningDataset = learningDatasetRepository.findById(learningDatasetId);
        if (existingLearningDataset.isPresent()) {
            learningDatasetRepository.delete(existingLearningDataset.get());
            return existingLearningDataset;
        } else {
            return Optional.empty();
        }
    }


    /**
     * Transactional Service method used to create both Transformations and Learning Datasets at the same time to avoid duplicating instances.
     * @param request Input request body in the form of a DTO.
     * @return
     */
    @Transactional
    public LearningDatasetandTransformationDTO createLearningDatasetAndTransformation(LearningDatasetandTransformationDTO request) {
        DatasetTransformation savedTransformation = datasetTransformationRepository.save(request.getDatasetTransformation());
        request.getLearningDataset().setDataTransformationId(savedTransformation.getDataTransformationId());

        // Find related study and set studyId field of the learning dataset
        Long relatedStudyId = this.studyService.findRelatedStudyByDatasetId(request.getLearningDataset().getDatasetId()).getId();
        request.getLearningDataset().setStudyId(relatedStudyId);

        LearningDataset savedLearningDataset = learningDatasetRepository.save(request.getLearningDataset());

        return new LearningDatasetandTransformationDTO(savedTransformation, savedLearningDataset);
    }

    /**
     * Updates both DatasetTransformation and LearningDataset in a single transaction.
     * @param transformation The updated DatasetTransformation
     * @param learningDataset The updated LearningDataset
     * @return An optional containing the updated LearningDataset and DatasetTransformation
     */
    @Transactional
    public Optional<LearningDatasetandTransformationDTO> updateLearningDatasetWithTransformation(
            DatasetTransformation transformation,
            LearningDataset learningDataset
    ) {
        Optional<DatasetTransformation> existingTransformation = datasetTransformationRepository.findById(transformation.getDataTransformationId());

        if (existingTransformation.isPresent()) {
            datasetTransformationRepository.save(transformation);

            learningDataset.setDataTransformationId(transformation.getDataTransformationId());
            Optional<LearningDataset> existingLearningDataset = learningDatasetRepository.findById(learningDataset.getLearningDatasetId());

            if (existingLearningDataset.isPresent()) {
                learningDatasetRepository.save(learningDataset);
                return Optional.of(new LearningDatasetandTransformationDTO(transformation, learningDataset));
            }
        }
        return Optional.empty();
    }
}
