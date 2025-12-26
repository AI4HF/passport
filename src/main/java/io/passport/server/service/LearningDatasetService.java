package io.passport.server.service;

import io.passport.server.model.DatasetTransformation;
import io.passport.server.model.LearningDataset;
import io.passport.server.model.LearningDatasetandTransformationDTO;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.DatasetTransformationRepository;
import io.passport.server.repository.LearningDatasetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for LearningDataset management.
 */
@Service
public class LearningDatasetService {

    /**
     * LearningDataset, Study and DatasetTransformation repo access for database management.
     */
    private final LearningDatasetRepository learningDatasetRepository;
    private final DatasetTransformationRepository datasetTransformationRepository;
    private final StudyService studyService;
    private final RoleCheckerService roleCheckerService;

    /**
     * Lazy service references for limited use in cascade validation
     */
    @Autowired @Lazy private LearningProcessDatasetService learningProcessDatasetService;

    @Autowired
    public LearningDatasetService(LearningDatasetRepository learningDatasetRepository,
                                  DatasetTransformationRepository datasetTransformationRepository,
                                  StudyService studyService,
                                  RoleCheckerService roleCheckerService) {
        this.learningDatasetRepository = learningDatasetRepository;
        this.datasetTransformationRepository = datasetTransformationRepository;
        this.studyService = studyService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Starts a validation chain of Learning Dataset and all of their children for cascades
     *
     * @param studyId Id of the Study
     * @param learningDatasetId Id of the Learning Dataset
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateLearningDatasetDeletion(String studyId, String learningDatasetId, Jwt principal) {
        List<ValidationResult> results = new ArrayList<>();

        results.add(learningProcessDatasetService.validateCascade(studyId, "LearningDataset", learningDatasetId, principal));

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
        List<LearningDataset> affectedLearningDatasets;

        switch (sourceResourceType) {
            case "Dataset":
                affectedLearningDatasets = learningDatasetRepository.findByDatasetId(sourceResourceId);
                break;
            case "Study":
                affectedLearningDatasets = learningDatasetRepository.findAllByStudyId(sourceResourceId);
                break;
            default:
                return new ValidationResult(1, "");
        }

        if (affectedLearningDatasets.isEmpty()) {
            return new ValidationResult(1, "");
        }

        List<ValidationResult> childResults = new ArrayList<>();
        boolean authorized = true;

        for (LearningDataset ld : affectedLearningDatasets) {
            boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                    studyId,
                    principal,
                    List.of(Role.DATA_ENGINEER, Role.DATA_SCIENTIST)
            );

            if (!hasPermission) {
                authorized = false;
                break;
            }

            childResults.add(validateLearningDatasetDeletion(studyId, ld.getLearningDatasetId(), principal));
        }

        if (!authorized) {
            return new ValidationResult(0, "LearningDataset");
        }

        childResults.add(new ValidationResult(1, "LearningDataset"));

        return ValidationResult.aggregate(childResults);
    }

    /**
     * Return all LearningDatasets by studyId
     * @param studyId ID of the study
     * @return
     */
    public List<LearningDataset> getAllLearningDatasetsByStudyId(String studyId) {
        return learningDatasetRepository.findAllByStudyId(studyId);
    }

    /**
     * Find LearningDatasets by dataTransformationId
     * @param dataTransformationId ID of the DataTransformation
     * @return
     */
    public List<LearningDataset> findByDataTransformationId(String dataTransformationId) {
        return learningDatasetRepository.findByDataTransformationId(dataTransformationId);
    }

    /**
     * Find LearningDatasets by datasetId
     * @param datasetId ID of the Dataset
     * @return
     */
    public List<LearningDataset> findByDatasetId(String datasetId) {
        return learningDatasetRepository.findByDatasetId(datasetId);
    }

    /**
     * Find a LearningDataset by learningDatasetId
     * @param learningDatasetId ID of the LearningDataset
     * @return
     */
    public Optional<LearningDataset> findLearningDatasetByLearningDatasetId(String learningDatasetId) {
        return learningDatasetRepository.findById(learningDatasetId);
    }

    /**
     * Delete a LearningDataset
     * @param learningDatasetId ID of LearningDataset to be deleted
     * @return
     */
    public Optional<LearningDataset> deleteLearningDataset(String learningDatasetId) {
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
        String relatedStudyId = this.studyService.findRelatedStudyByDatasetId(request.getLearningDataset().getDatasetId()).getId();
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
