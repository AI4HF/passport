package io.passport.server.service;

import io.passport.server.model.ModelEvaluationDataset;
import io.passport.server.model.ModelEvaluationDatasetId;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.ModelEvaluationDatasetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for ModelEvaluationDataset management.
 */
@Service
public class ModelEvaluationDatasetService {

    /**
     * ModelEvaluationDataset repo access for database management.
     */
    private final ModelEvaluationDatasetRepository modelEvaluationDatasetRepository;
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public ModelEvaluationDatasetService(ModelEvaluationDatasetRepository modelEvaluationDatasetRepository,
                                         RoleCheckerService roleCheckerService) {
        this.modelEvaluationDatasetRepository = modelEvaluationDatasetRepository;
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
        List<ModelEvaluationDataset> affectedLinks;

        switch (sourceResourceType) {
            case "ModelEvaluation":
                affectedLinks = modelEvaluationDatasetRepository.findByIdModelEvaluationId(sourceResourceId);
                break;
            case "LearningDataset":
                affectedLinks = modelEvaluationDatasetRepository.findByIdLearningDatasetId(sourceResourceId);
                break;
            default:
                return new ValidationResult(true, "");
        }

        if (affectedLinks.isEmpty()) {
            return new ValidationResult(true, "");
        }

        boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                studyId,
                principal,
                List.of(Role.DATA_SCIENTIST)
        );

        if (!hasPermission) {
            return new ValidationResult(false, "ModelEvaluationDataset");
        }

        return new ValidationResult(true, "ModelEvaluationDataset");
    }

    /**
     * Find ModelEvaluationDatasets by modelEvaluationId
     * @param modelEvaluationId ID of the ModelEvaluation
     * @return
     */
    public List<ModelEvaluationDataset> findByModelEvaluationId(String modelEvaluationId) {
        return modelEvaluationDatasetRepository.findByIdModelEvaluationId(modelEvaluationId);
    }

    /**
     * Find ModelEvaluationDatasets by learningDatasetId
     * @param learningDatasetId ID of the LearningDataset
     * @return
     */
    public List<ModelEvaluationDataset> findByLearningDatasetId(String learningDatasetId) {
        return modelEvaluationDatasetRepository.findByIdLearningDatasetId(learningDatasetId);
    }

    /**
     * Find a ModelEvaluationDataset by composite id
     * @param modelEvaluationDatasetId composite ID of the ModelEvaluationDataset
     * @return
     */
    public Optional<ModelEvaluationDataset> findModelEvaluationDatasetById(ModelEvaluationDatasetId modelEvaluationDatasetId) {
        return modelEvaluationDatasetRepository.findById(modelEvaluationDatasetId);
    }

    /**
     * Save a ModelEvaluationDataset
     * @param modelEvaluationDataset ModelEvaluationDataset to be saved
     * @return
     */
    public ModelEvaluationDataset saveModelEvaluationDataset(ModelEvaluationDataset modelEvaluationDataset) {
        return modelEvaluationDatasetRepository.save(modelEvaluationDataset);
    }

    /**
     * Update a ModelEvaluationDataset
     * @param modelEvaluationDatasetId composite ID of the ModelEvaluationDataset
     * @param updatedModelEvaluationDataset ModelEvaluationDataset to be updated
     * @return
     */
    public Optional<ModelEvaluationDataset> updateModelEvaluationDataset(ModelEvaluationDatasetId modelEvaluationDatasetId,
                                                                        ModelEvaluationDataset updatedModelEvaluationDataset) {
        Optional<ModelEvaluationDataset> oldLink = modelEvaluationDatasetRepository.findById(modelEvaluationDatasetId);
        if (oldLink.isPresent()) {
            ModelEvaluationDataset link = oldLink.get();
            link.setWeight(updatedModelEvaluationDataset.getWeight());
            link.setDescription(updatedModelEvaluationDataset.getDescription());
            ModelEvaluationDataset savedLink = modelEvaluationDatasetRepository.save(link);
            return Optional.of(savedLink);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a ModelEvaluationDataset
     * @param modelEvaluationDatasetId composite ID of ModelEvaluationDataset to be deleted
     * @return
     */
    public Optional<ModelEvaluationDataset> deleteModelEvaluationDataset(ModelEvaluationDatasetId modelEvaluationDatasetId) {
        Optional<ModelEvaluationDataset> existingLink = modelEvaluationDatasetRepository.findById(modelEvaluationDatasetId);
        if (existingLink.isPresent()) {
            modelEvaluationDatasetRepository.delete(existingLink.get());
            return existingLink;
        } else {
            return Optional.empty();
        }
    }
}
