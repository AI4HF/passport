package io.passport.server.service;

import io.passport.server.model.ModelParameter;
import io.passport.server.model.ModelParameterId;
import io.passport.server.repository.ModelParameterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for ModelParameter management.
 */
@Service
public class ModelParameterService {

    /**
     * ModelParameter repo access for database management.
     */
    private final ModelParameterRepository modelParameterRepository;

    @Autowired
    public ModelParameterService(ModelParameterRepository modelParameterRepository) {
        this.modelParameterRepository = modelParameterRepository;
    }

    /**
     * Return all ModelParameters
     * @return
     */
    public List<ModelParameter> getAllModelParameters() {
        return modelParameterRepository.findAll();
    }

    /**
     * Find ModelParameters by modelId
     * @param modelId ID of the Model
     * @return
     */
    public List<ModelParameter> findByModelId(Long modelId) {
        return modelParameterRepository.findByIdModelId(modelId);
    }

    /**
     * Find ModelParameters by parameterId
     * @param parameterId ID of the Parameter
     * @return
     */
    public List<ModelParameter> findByParameterId(Long parameterId) {
        return modelParameterRepository.findByIdParameterId(parameterId);
    }

    /**
     * Find a ModelParameter by composite id
     * @param modelParameterId composite ID of the ModelParameter
     * @return
     */
    public Optional<ModelParameter> findModelParameterById(ModelParameterId modelParameterId) {
        return modelParameterRepository.findById(modelParameterId);
    }

    /**
     * Save a ModelParameter
     * @param modelParameter ModelParameter to be saved
     * @return
     */
    public ModelParameter saveModelParameter(ModelParameter modelParameter) {
        return modelParameterRepository.save(modelParameter);
    }

    /**
     * Update a ModelParameter
     * @param modelParameterId composite ID of the ModelParameter
     * @param updatedModelParameter ModelParameter to be updated
     * @return
     */
    public Optional<ModelParameter> updateModelParameter(ModelParameterId modelParameterId, ModelParameter updatedModelParameter) {
        Optional<ModelParameter> oldModelParameter = modelParameterRepository.findById(modelParameterId);
        if (oldModelParameter.isPresent()) {
            ModelParameter modelParameter = oldModelParameter.get();
            modelParameter.setType(updatedModelParameter.getType());
            modelParameter.setValue(updatedModelParameter.getValue());
            ModelParameter savedModelParameter = modelParameterRepository.save(modelParameter);
            return Optional.of(savedModelParameter);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a ModelParameter
     * @param modelParameterId composite ID of ModelParameter to be deleted
     * @return
     */
    public Optional<ModelParameter> deleteModelParameter(ModelParameterId modelParameterId) {
        Optional<ModelParameter> existingModelParameter = modelParameterRepository.findById(modelParameterId);
        if (existingModelParameter.isPresent()) {
            modelParameterRepository.delete(existingModelParameter.get());
            return existingModelParameter;
        } else {
            return Optional.empty();
        }
    }

}
