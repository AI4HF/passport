package io.passport.server.service;

import io.passport.server.model.Model;
import io.passport.server.repository.ModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service class for model management.
 */
@Service
public class ModelService {

    /**
     * Model repo access for database management.
     */
    private final ModelRepository modelRepository;

    @Autowired
    public ModelService(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    /**
     * Get all models
     */
    public List<Model> getAllModels(){
        return modelRepository.findAll();
    }

    /**
     * Get all models by studyId
     * @param studyId ID of the study
     */
    public List<Model> getAllModelsByStudyId(Long studyId){
        return modelRepository.findByStudyId(studyId);
    }

    /**
     * Find a model by modelId
     * @param modelId ID of the model
     * @return
     */
    public Optional<Model> findModelById(Long modelId) {
        return modelRepository.findById(modelId);
    }

    /**
     * Save a model
     * @param model model to be saved
     * @return
     */
    public Model saveModel(Model model) {
        model.setCreatedAt(Instant.now());
        model.setLastUpdatedAt(Instant.now());
        return modelRepository.save(model);
    }

    /**
     * Update a model
     * @param modelId ID of the model
     * @param updatedModel model to be updated
     * @return
     */
    public Optional<Model> updateModel(Long modelId, Model updatedModel) {
        Optional<Model> oldModel = modelRepository.findById(modelId);
        if (oldModel.isPresent()) {
            Model model = oldModel.get();
            model.setModelId(modelId);
            model.setLearningProcessId(updatedModel.getLearningProcessId());
            model.setName(updatedModel.getName());
            model.setVersion(updatedModel.getVersion());
            model.setTag(updatedModel.getTag());
            model.setModelType(updatedModel.getModelType());
            model.setProductIdentifier(updatedModel.getProductIdentifier());
            model.setTrlLevel(updatedModel.getTrlLevel());
            model.setLicense(updatedModel.getLicense());
            model.setPrimaryUse(updatedModel.getPrimaryUse());
            model.setSecondaryUse(updatedModel.getSecondaryUse());
            model.setIntendedUsers(updatedModel.getIntendedUsers());
            model.setCounterIndications(updatedModel.getCounterIndications());
            model.setEthicalConsiderations(updatedModel.getEthicalConsiderations());
            model.setLimitations(updatedModel.getLimitations());
            model.setFairnessConstraints(updatedModel.getFairnessConstraints());
            model.setLastUpdatedAt(Instant.now());
            model.setLastUpdatedBy(model.getLastUpdatedBy());
            Model savedModel = modelRepository.save(model);
            return Optional.of(savedModel);
        }else{
            return Optional.empty();
        }
    }

    /**
     * Delete a model
     * @param modelId ID of model to be deleted
     * @return
     */
    public Optional<Model> deleteModel(Long modelId) {
        Optional<Model> existingModel = modelRepository.findById(modelId);
        if (existingModel.isPresent()) {
            modelRepository.delete(existingModel.get());
            return existingModel;
        } else {
            return Optional.empty();
        }
    }

}
