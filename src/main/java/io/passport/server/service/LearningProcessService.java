package io.passport.server.service;

import io.passport.server.model.LearningProcess;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.LearningProcessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for learning process management.
 */
@Service
public class LearningProcessService {

    /**
     * LearningProcess repo access for database management.
     */
    private final LearningProcessRepository learningProcessRepository;
    private final RoleCheckerService roleCheckerService;

    /**
     * Lazy service references for limited use in cascade validation
     */
    @Autowired @Lazy private LearningProcessDatasetService learningProcessDatasetService;
    @Autowired @Lazy private LearningStageService learningStageService;
    @Autowired @Lazy private LearningProcessParameterService learningProcessParameterService;
    @Autowired @Lazy private ModelService modelService;

    @Autowired
    public LearningProcessService(LearningProcessRepository learningProcessRepository,
                                  RoleCheckerService roleCheckerService) {
        this.learningProcessRepository = learningProcessRepository;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Starts a validation chain of Learning Process and all of their children for cascades
     *
     * @param studyId Id of the Study
     * @param learningProcessId Id of the Learning Process
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateLearningProcessDeletion(String studyId, String learningProcessId, Jwt principal) {
        List<ValidationResult> results = new ArrayList<>();

        results.add(learningProcessDatasetService.validateCascade(studyId, "LearningProcess", learningProcessId, principal));
        results.add(learningStageService.validateCascade(studyId, "LearningProcess", learningProcessId, principal));
        results.add(learningProcessParameterService.validateCascade(studyId, "LearningProcess", learningProcessId, principal));
        results.add(modelService.validateCascade(studyId, "LearningProcess", learningProcessId, principal));

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
        List<LearningProcess> affectedProcesses;

        switch (sourceResourceType) {
            case "Study":
                affectedProcesses = learningProcessRepository.findAllByStudyId(sourceResourceId);
                break;
            case "Implementation":
                affectedProcesses = learningProcessRepository.findByImplementationId(sourceResourceId);
                break;
            default:
                return new ValidationResult(1, "");
        }

        if (affectedProcesses.isEmpty()) {
            return new ValidationResult(1, "");
        }

        List<ValidationResult> childResults = new ArrayList<>();
        boolean authorized = true;

        for (LearningProcess lp : affectedProcesses) {
            boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                    studyId,
                    principal,
                    List.of(Role.DATA_SCIENTIST)
            );

            if (!hasPermission) {
                authorized = false;
                break;
            }

            childResults.add(validateLearningProcessDeletion(studyId, lp.getLearningProcessId(), principal));
        }

        if (!authorized) {
            return new ValidationResult(0, "LearningProcess");
        }

        childResults.add(new ValidationResult(1, "LearningProcess"));

        return ValidationResult.aggregate(childResults);
    }

    /**
     * Return all learning processes
     * @return
     */
    public List<LearningProcess> getAllLearningProcesses() {
        return learningProcessRepository.findAll();
    }

    /**
     * Return all LearningProcess by studyId
     * @param studyId ID of the study
     * @return
     */
    public List<LearningProcess> getAllLearningProcessByStudyId(String studyId) {
        return learningProcessRepository.findAllByStudyId(studyId);
    }

    /**
     * Find a learning process by learningProcessId
     * @param learningProcessId ID of the learning process
     * @return
     */
    public Optional<LearningProcess> findLearningProcessById(String learningProcessId) {
        return learningProcessRepository.findById(learningProcessId);
    }

    /**
     * Save a learning process
     * @param learningProcess learning process to be saved
     * @return
     */
    public LearningProcess saveLearningProcess(LearningProcess learningProcess) {
        return learningProcessRepository.save(learningProcess);
    }

    /**
     * Update a learning process
     * @param learningProcessId ID of the learning process
     * @param updatedLearningProcess learning process to be updated
     * @return
     */
    public Optional<LearningProcess> updateLearningProcess(String learningProcessId, LearningProcess updatedLearningProcess) {
        Optional<LearningProcess> oldLearningProcess = learningProcessRepository.findById(learningProcessId);
        if (oldLearningProcess.isPresent()) {
            LearningProcess learningProcess = oldLearningProcess.get();
            learningProcess.setImplementationId(updatedLearningProcess.getImplementationId());
            learningProcess.setDescription(updatedLearningProcess.getDescription());
            LearningProcess savedLearningProcess = learningProcessRepository.save(learningProcess);
            return Optional.of(savedLearningProcess);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a learning process
     * @param learningProcessId ID of learning process to be deleted
     * @return
     */
    public Optional<LearningProcess> deleteLearningProcess(String learningProcessId) {
        Optional<LearningProcess> existingLearningProcess = learningProcessRepository.findById(learningProcessId);
        if (existingLearningProcess.isPresent()) {
            learningProcessRepository.delete(existingLearningProcess.get());
            return existingLearningProcess;
        } else {
            return Optional.empty();
        }
    }

}
