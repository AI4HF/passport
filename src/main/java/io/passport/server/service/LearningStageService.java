package io.passport.server.service;

import io.passport.server.model.LearningStage;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.LearningStageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for learning stage management.
 */
@Service
public class LearningStageService {

    /**
     * LearningStage repo access for database management.
     */
    private final LearningStageRepository learningStageRepository;
    private final RoleCheckerService roleCheckerService;

    /**
     * Lazy service references for limited use in cascade validation
     */
    @Autowired @Lazy private LearningStageParameterService learningStageParameterService;

    @Autowired
    public LearningStageService(LearningStageRepository learningStageRepository,
                                RoleCheckerService roleCheckerService) {
        this.learningStageRepository = learningStageRepository;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Starts a validation chain of Learning Stage and all of their children for cascades
     *
     * @param studyId Id of the Study
     * @param learningStageId Id of the Learning Stage
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateLearningStageDeletion(String studyId, String learningStageId, Jwt principal) {
        List<ValidationResult> results = new ArrayList<>();

        results.add(learningStageParameterService.validateCascade(studyId, "LearningStage", learningStageId, principal));

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
        List<LearningStage> affectedStages;

        switch (sourceResourceType) {
            case "LearningProcess":
                affectedStages = learningStageRepository.findByLearningProcessId(sourceResourceId);
                break;
            default:
                return new ValidationResult(true, "");
        }

        if (affectedStages.isEmpty()) {
            return new ValidationResult(true, "");
        }

        List<ValidationResult> childResults = new ArrayList<>();
        boolean authorized = true;

        for (LearningStage stage : affectedStages) {
            boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                    studyId,
                    principal,
                    List.of(Role.DATA_SCIENTIST)
            );

            if (!hasPermission) {
                authorized = false;
                break;
            }

            childResults.add(validateLearningStageDeletion(studyId, stage.getLearningStageId(), principal));
        }

        if (!authorized) {
            return new ValidationResult(false, "LearningStage");
        }

        childResults.add(new ValidationResult(true, "LearningStage"));

        return ValidationResult.aggregate(childResults);
    }

    /**
     * Return all learning stages
     * @return
     */
    public List<LearningStage> getAllLearningStages() {
        return learningStageRepository.findAll();
    }

    /**
     * Return all learning stages by Learning Process ID
     * @param learningProcessId ID of the learning process
     * @return
     */
    public List<LearningStage> findLearningStagesByProcessId(String learningProcessId) {
        return learningStageRepository.findByLearningProcessId(learningProcessId);
    }

    /**
     * Find a learning stage by learningStageId
     * @param learningStageId ID of the learning stage
     * @return
     */
    public Optional<LearningStage> findLearningStageById(String learningStageId) {
        return learningStageRepository.findById(learningStageId);
    }

    /**
     * Save a learning stage
     * @param learningStage learning stage to be saved
     * @return
     */
    public LearningStage saveLearningStage(LearningStage learningStage) {
        return learningStageRepository.save(learningStage);
    }

    /**
     * Update a learning stage
     * @param learningStageId ID of the learning stage
     * @param updatedLearningStage learning stage to be updated
     * @return
     */
    public Optional<LearningStage> updateLearningStage(String learningStageId, LearningStage updatedLearningStage) {
        Optional<LearningStage> oldLearningStage = learningStageRepository.findById(learningStageId);
        if (oldLearningStage.isPresent()) {
            LearningStage learningStage = oldLearningStage.get();
            learningStage.setLearningProcessId(updatedLearningStage.getLearningProcessId());
            learningStage.setLearningStageName(updatedLearningStage.getLearningStageName());
            learningStage.setDescription(updatedLearningStage.getDescription());
            learningStage.setDatasetPercentage(updatedLearningStage.getDatasetPercentage());
            LearningStage savedLearningStage = learningStageRepository.save(learningStage);
            return Optional.of(savedLearningStage);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a learning stage
     * @param learningStageId ID of learning stage to be deleted
     * @return
     */
    public Optional<LearningStage> deleteLearningStage(String learningStageId) {
        Optional<LearningStage> existingStage = learningStageRepository.findById(learningStageId);
        if (existingStage.isPresent()) {
            learningStageRepository.delete(existingStage.get());
            return existingStage;
        } else {
            return Optional.empty();
        }
    }

}
