package io.passport.server.service;

import io.passport.server.model.Parameter;
import io.passport.server.repository.ParameterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for parameter management.
 */
@Service
public class ParameterService {

    /**
     * Parameter repo access for database management.
     */
    private final ParameterRepository parameterRepository;

    @Autowired
    public ParameterService(ParameterRepository parameterRepository) {
        this.parameterRepository = parameterRepository;
    }

    /**
     * Get all parameters
     */
    public List<Parameter> getAllParameters() {
        return parameterRepository.findAll();
    }

    /**
     * Find parameters by studyId
     * @param studyId The ID of the study
     * @return
     */
    public List<Parameter> findParametersByStudyId(Long studyId) {
        return parameterRepository.findAllByStudyId(studyId);
    }

    /**
     * Find a parameter by parameterId
     * @param parameterId ID of the parameter
     * @return
     */
    public Optional<Parameter> findParameterById(Long parameterId) {
        return parameterRepository.findById(parameterId);
    }

    /**
     * Save a parameter
     * @param parameter parameter to be saved
     * @return
     */
    public Parameter saveParameter(Parameter parameter) {
        return parameterRepository.save(parameter);
    }

    /**
     * Update a parameter
     * @param parameterId ID of the parameter
     * @param updatedParameter parameter to be updated
     * @return
     */
    public Optional<Parameter> updateParameter(Long parameterId, Parameter updatedParameter) {
        Optional<Parameter> oldParameter = parameterRepository.findById(parameterId);
        if (oldParameter.isPresent()) {
            Parameter parameter = oldParameter.get();
            parameter.setName(updatedParameter.getName());
            parameter.setDescription(updatedParameter.getDescription());
            parameter.setDataType(updatedParameter.getDataType());
            Parameter savedParameter = parameterRepository.save(parameter);
            return Optional.of(savedParameter);
        }else{
            return Optional.empty();
        }
    }

    /**
     * Delete a parameter
     * @param parameterId ID of parameter to be deleted
     * @return
     */
    public Optional<Parameter> deleteParameter(Long parameterId) {
        Optional<Parameter> existingParameter = parameterRepository.findById(parameterId);
        if (existingParameter.isPresent()) {
            parameterRepository.delete(existingParameter.get());
            return existingParameter;
        } else {
            return Optional.empty();
        }
    }

}
