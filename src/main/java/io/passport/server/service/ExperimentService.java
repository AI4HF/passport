package io.passport.server.service;

import io.passport.server.model.Experiment;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.ExperimentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for experiment management.
 */
@Service
public class ExperimentService {

    /**
     * Experiment repo access for database management.
     */
    public final ExperimentRepository experimentRepository;
    private final RoleCheckerService roleCheckerService;

    /**
     * Lazy service references for limited use in cascade validation
     */
    @Autowired @Lazy private ModelService modelService;
    @Autowired @Lazy private FeatureSetService featureSetService;

    @Autowired
    public ExperimentService(ExperimentRepository experimentRepository, RoleCheckerService roleCheckerService) {
        this.experimentRepository = experimentRepository;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Starts a validation chain of Experiments and all of their children for cascades
     *
     * @param studyId Id of the Study
     * @param experimentId Id of the Experiment
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateExperimentDeletion(String studyId, String experimentId, Jwt principal) {
        List<ValidationResult> results = new ArrayList<>();

        results.add(modelService.validateCascade(studyId, "Experiment", experimentId, principal));
        results.add(featureSetService.validateCascade(studyId, "Experiment", experimentId, principal));

        return ValidationResult.aggregate(results);
    }

    /**
     * Validate all Experiments for cascades that are removed during an Experiment replacement
     *
     *  @param studyId Id of the Study
     * @param incoming List of overwriting Experiment list
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateExperimentReplacement(String studyId, List<Experiment> incoming, Jwt principal) {
        List<Experiment> existingExperiments = experimentRepository.findByStudyId(studyId);

        Set<String> incomingIds = incoming.stream()
                .map(Experiment::getExperimentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Experiment> toDelete = existingExperiments.stream()
                .filter(exp -> !incomingIds.contains(exp.getExperimentId()))
                .collect(Collectors.toList());

        if (toDelete.isEmpty()) {
            return new ValidationResult(1, "");
        }

        List<ValidationResult> validationResults = new ArrayList<>();
        for (Experiment expToDelete : toDelete) {
            validationResults.add(validateExperimentDeletion(studyId, expToDelete.getExperimentId(), principal));
        }

        return ValidationResult.aggregate(validationResults);
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
        List<Experiment> affectedExperiments;

        switch (sourceResourceType) {
            case "Study":
                affectedExperiments = experimentRepository.findByStudyId(sourceResourceId);
                break;
            default:
                return new ValidationResult(1, "");
        }

        if (affectedExperiments.isEmpty()) {
            return new ValidationResult(1, "");
        }

        List<ValidationResult> childResults = new ArrayList<>();
        boolean authorizedForExperiments = true;

        for (Experiment exp : affectedExperiments) {
            boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                    studyId,
                    principal,
                    List.of(Role.STUDY_OWNER)
            );

            if (!hasPermission) {
                authorizedForExperiments = false;
                break;
            }

            childResults.add(validateExperimentDeletion(studyId, exp.getExperimentId(), principal));
        }

        if (!authorizedForExperiments) {
            return new ValidationResult(0, "Experiment");
        }

        childResults.add(new ValidationResult(1, "Experiment"));

        return ValidationResult.aggregate(childResults);
    }

    /**
     * Find an experiment by studyId
     * @param studyId ID of the study
     * @return
     */
    public List<Experiment> findExperimentByStudyId(String studyId) {
        return this.experimentRepository.findByStudyId(studyId);
    }

    /**
     * Overwrite all Experiment entries for a Study
     * @param studyId ID of the study
     * @param incoming Collection of Experiments to be overwritten as
     * @return Final state of overwritten Experiments
     */
    @Transactional
    public List<Experiment> replaceExperiments(String studyId, List<Experiment> incoming) {
        if (incoming.isEmpty()) {
            experimentRepository.deleteAllByStudyId(studyId);
            return Collections.emptyList();
        }

        Set<String> incomingIds = incoming.stream()
                .map(Experiment::getExperimentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (incomingIds.isEmpty()) experimentRepository.deleteAllByStudyId(studyId);
        else experimentRepository.deleteByStudyIdAndExperimentIdNotIn(studyId, incomingIds);

        List<Experiment> toSave = incoming.stream()
                .map(exp -> {
                    Experiment e = new Experiment();
                    e.setExperimentId(exp.getExperimentId());
                    e.setStudyId(studyId);
                    e.setResearchQuestion(exp.getResearchQuestion());
                    return e;
                })
                .collect(Collectors.toList());

        return experimentRepository.saveAll(toSave);
    }
}
