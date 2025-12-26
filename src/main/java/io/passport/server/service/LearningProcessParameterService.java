package io.passport.server.service;

import io.passport.server.model.LearningProcessParameter;
import io.passport.server.model.LearningProcessParameterId;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.LearningProcessParameterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for LearningProcessParameter management.
 */
@Service
public class LearningProcessParameterService {

    /**
     * LearningProcessParameter repo access for database management.
     */
    private final LearningProcessParameterRepository learningProcessParameterRepository;
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public LearningProcessParameterService(LearningProcessParameterRepository learningProcessParameterRepository,
                                           RoleCheckerService roleCheckerService) {
        this.learningProcessParameterRepository = learningProcessParameterRepository;
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
        List<LearningProcessParameter> affectedRecords;

        switch (sourceResourceType) {
            case "LearningProcess":
                affectedRecords = learningProcessParameterRepository.findByIdLearningProcessId(sourceResourceId);
                break;
            case "Parameter":
                affectedRecords = learningProcessParameterRepository.findByIdParameterId(sourceResourceId);
                break;
            default:
                return new ValidationResult(1, "");
        }

        if (affectedRecords.isEmpty()) {
            return new ValidationResult(1, "");
        }

        boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                studyId,
                principal,
                List.of(Role.DATA_SCIENTIST)
        );

        if (!hasPermission) {
            return new ValidationResult(0, "LearningProcessParameter");
        }

        return new ValidationResult(1, "LearningProcessParameter");
    }

    /**
     * Return all LearningProcessParameters
     * @return
     */
    public List<LearningProcessParameter> getAllLearningProcessParameters() {
        return learningProcessParameterRepository.findAll();
    }

    /**
     * Find LearningProcessParameters by learningProcessId
     * @param learningProcessId ID of the LearningProcess
     * @return
     */
    public List<LearningProcessParameter> findByLearningProcessId(String learningProcessId) {
        return learningProcessParameterRepository.findByIdLearningProcessId(learningProcessId);
    }

    /**
     * Find LearningProcessParameters by parameterId
     * @param parameterId ID of the Parameter
     * @return
     */
    public List<LearningProcessParameter> findByParameterId(String parameterId) {
        return learningProcessParameterRepository.findByIdParameterId(parameterId);
    }

    /**
     * Find a LearningProcessParameter by composite id
     * @param learningProcessParameterId composite ID of the LearningProcessParameter
     * @return
     */
    public Optional<LearningProcessParameter> findLearningProcessParameterById(LearningProcessParameterId learningProcessParameterId) {
        return learningProcessParameterRepository.findById(learningProcessParameterId);
    }

    /**
     * Save a LearningProcessParameter
     * @param learningProcessParameter LearningProcessParameter to be saved
     * @return
     */
    public LearningProcessParameter saveLearningProcessParameter(LearningProcessParameter learningProcessParameter) {
        return learningProcessParameterRepository.save(learningProcessParameter);
    }

    /**
     * Update a LearningProcessParameter
     * @param learningProcessParameterId composite ID of the LearningProcessParameter
     * @param updatedLearningProcessParameter LearningProcessParameter to be updated
     * @return
     */
    public Optional<LearningProcessParameter> updateLearningProcessParameter(LearningProcessParameterId learningProcessParameterId, LearningProcessParameter updatedLearningProcessParameter) {
        Optional<LearningProcessParameter> oldLearningProcessParameter = learningProcessParameterRepository.findById(learningProcessParameterId);
        if (oldLearningProcessParameter.isPresent()) {
            LearningProcessParameter learningProcessParameter = oldLearningProcessParameter.get();
            learningProcessParameter.setType(updatedLearningProcessParameter.getType());
            learningProcessParameter.setValue(updatedLearningProcessParameter.getValue());
            LearningProcessParameter savedLearningProcessParameter = learningProcessParameterRepository.save(learningProcessParameter);
            return Optional.of(savedLearningProcessParameter);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a LearningProcessParameter
     * @param learningProcessParameterId composite ID of LearningProcessParameter to be deleted
     * @return
     */
    public Optional<LearningProcessParameter> deleteLearningProcessParameter(LearningProcessParameterId learningProcessParameterId) {
        Optional<LearningProcessParameter> existingProcessParameter = learningProcessParameterRepository.findById(learningProcessParameterId);
        if (existingProcessParameter.isPresent()) {
            learningProcessParameterRepository.delete(existingProcessParameter.get());
            return existingProcessParameter;
        } else {
            return Optional.empty();
        }
    }

}
