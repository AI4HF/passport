package io.passport.server.service;

import io.passport.server.model.ModelFigure;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.ModelFigureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for ModelFigure management.
 */
@Service
public class ModelFigureService {

    /**
     * ModelFigure repo access for database management.
     */
    private final ModelFigureRepository modelFigureRepository;
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public ModelFigureService(ModelFigureRepository modelFigureRepository,
                              RoleCheckerService roleCheckerService) {
        this.modelFigureRepository = modelFigureRepository;
        this.roleCheckerService = roleCheckerService;
    }

    public ValidationResult validateCascade(String studyId, String sourceResourceType, String sourceResourceId, Jwt principal) {
        List<ModelFigure> affectedFigures;

        switch (sourceResourceType) {
            case "Model":
                affectedFigures = modelFigureRepository.findByModelId(sourceResourceId);
                break;
            default:
                return new ValidationResult(1, "");
        }

        if (affectedFigures.isEmpty()) {
            return new ValidationResult(1, "");
        }

        boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                studyId,
                principal,
                List.of(Role.DATA_SCIENTIST)
        );

        if (!hasPermission) {
            return new ValidationResult(0, "ModelFigure");
        }

        return new ValidationResult(1, "ModelFigure");
    }

    /**
     * Return all ModelFigures
     * @return
     */
    public List<ModelFigure> getAllModelFigures() {
        return modelFigureRepository.findAll();
    }

    /**
     * Find ModelFigures by modelId
     * @param modelId ID of the Model
     * @return
     */
    public List<ModelFigure> findByModelId(String modelId) {
        return modelFigureRepository.findByModelId(modelId);
    }

    /**
     * Find a ModelFigure by ID
     * @param figureId the ID of the ModelFigure
     * @return
     */
    public Optional<ModelFigure> findModelFigureById(String figureId) {
        return modelFigureRepository.findById(figureId);
    }

    /**
     * Save a ModelFigure
     * @param modelFigure ModelFigure to be saved
     * @return
     */
    public ModelFigure saveModelFigure(ModelFigure modelFigure) {
        return modelFigureRepository.save(modelFigure);
    }

    /**
     * Update a ModelFigure
     * @param figureId the ID of the ModelFigure
     * @param updatedModelFigure ModelFigure to be updated
     * @return
     */
    public Optional<ModelFigure> updateModelFigure(String figureId, ModelFigure updatedModelFigure) {
        Optional<ModelFigure> oldModelFigure = modelFigureRepository.findById(figureId);
        if (oldModelFigure.isPresent()) {
            ModelFigure modelFigure = oldModelFigure.get();
            modelFigure.setModelId(updatedModelFigure.getModelId());
            modelFigure.setImageBase64(updatedModelFigure.getImageBase64());
            ModelFigure savedModelFigure = modelFigureRepository.save(modelFigure);
            return Optional.of(savedModelFigure);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a ModelFigure
     * @param figureId the ID of ModelFigure to be deleted
     * @return
     */
    public Optional<ModelFigure> deleteModelFigure(String figureId) {
        Optional<ModelFigure> existingModelFigure = modelFigureRepository.findById(figureId);
        if (existingModelFigure.isPresent()) {
            modelFigureRepository.delete(existingModelFigure.get());
            return existingModelFigure;
        } else {
            return Optional.empty();
        }
    }
}