package io.passport.server.service;

import io.passport.server.model.Parameter;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.ParameterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    private final RoleCheckerService roleCheckerService;

    /**
     * Lazy service references for limited use in cascade validation
     */
    @Autowired @Lazy private LearningProcessParameterService learningProcessParameterService;
    @Autowired @Lazy private LearningStageParameterService learningStageParameterService;
    @Autowired @Lazy private ModelParameterService modelParameterService;

    @Autowired
    public ParameterService(ParameterRepository parameterRepository,
                            RoleCheckerService roleCheckerService) {
        this.parameterRepository = parameterRepository;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Starts a validation chain of Parameter and all of their children for cascades
     *
     * @param studyId Id of the Study
     * @param parameterId Id of the Parameter
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateParameterDeletion(String studyId, String parameterId, Jwt principal) {
        List<ValidationResult> results = new ArrayList<>();

        results.add(learningProcessParameterService.validateCascade(studyId, "Parameter", parameterId, principal));
        results.add(learningStageParameterService.validateCascade(studyId, "Parameter", parameterId, principal));
        results.add(modelParameterService.validateCascade(studyId, "Parameter", parameterId, principal));

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
        List<Parameter> affectedParameters;

        switch (sourceResourceType) {
            case "Study":
                affectedParameters = parameterRepository.findAllByStudyId(sourceResourceId);
                break;
            default:
                return new ValidationResult(true, "");
        }

        if (affectedParameters.isEmpty()) {
            return new ValidationResult(true, "");
        }

        List<ValidationResult> childResults = new ArrayList<>();
        boolean authorized = true;

        for (Parameter p : affectedParameters) {
            boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                    studyId,
                    principal,
                    List.of(Role.DATA_SCIENTIST)
            );

            if (!hasPermission) {
                authorized = false;
                break;
            }

            childResults.add(validateParameterDeletion(studyId, p.getParameterId(), principal));
        }

        if (!authorized) {
            return new ValidationResult(false, "Parameter");
        }

        childResults.add(new ValidationResult(true, "Parameter"));

        return ValidationResult.aggregate(childResults);
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
    public List<Parameter> findParametersByStudyId(String studyId) {
        return parameterRepository.findAllByStudyId(studyId);
    }

    /**
     * Find a parameter by parameterId
     * @param parameterId ID of the parameter
     * @return
     */
    public Optional<Parameter> findParameterById(String parameterId) {
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
    public Optional<Parameter> updateParameter(String parameterId, Parameter updatedParameter) {
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
    public Optional<Parameter> deleteParameter(String parameterId) {
        Optional<Parameter> existingParameter = parameterRepository.findById(parameterId);
        if (existingParameter.isPresent()) {
            parameterRepository.delete(existingParameter.get());
            return existingParameter;
        } else {
            return Optional.empty();
        }
    }

}
