package io.passport.server.service;

import io.passport.server.model.Study;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.StudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for study management.
 */
@Service
public class StudyService {

    /**
     * Study repo access for database management.
     */
    private final StudyRepository studyRepository;

    /**
     * Lazy service references for limited use in cascade validation
     */
    @Autowired @Lazy private ExperimentService experimentService;
    @Autowired @Lazy private PopulationService populationService;
    @Autowired @Lazy private LearningProcessService learningProcessService;
    @Autowired @Lazy private ParameterService parameterService;
    @Autowired @Lazy private SurveyService surveyService;
    @Autowired @Lazy private StudyPersonnelService studyPersonnelService;
    @Autowired @Lazy private StudyOrganizationService studyOrganizationService;
    @Autowired @Lazy private LinkedArticleService linkedArticleService;

    @Autowired
    public StudyService(StudyRepository studyRepository) {
        this.studyRepository = studyRepository;
    }

    /**
     * Starts a validation chain of Study and all of their children for cascades
     *
     * @param studyId Id of the Study
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateStudyDeletion(String studyId, Jwt principal) {
        List<ValidationResult> results = new ArrayList<>();

        results.add(experimentService.validateCascade(studyId, "Study", studyId, principal));
        results.add(populationService.validateCascade(studyId, "Study", studyId, principal));
        results.add(learningProcessService.validateCascade(studyId, "Study", studyId, principal));
        results.add(parameterService.validateCascade(studyId, "Study", studyId, principal));
        results.add(surveyService.validateCascade(studyId, "Study", studyId, principal));
        results.add(studyPersonnelService.validateCascade(studyId, "Study", studyId, principal));
        results.add(studyOrganizationService.validateCascade(studyId, "Study", studyId, principal));
        results.add(linkedArticleService.validateCascade(studyId, "Study", studyId, principal));

        return ValidationResult.aggregate(results);
    }

    /**
     * Return all studies
     * @return
     */
    public List<Study> getAllStudies() {
        return studyRepository.findAll();
    }

    /**
     * Find a study by studyId
     * @param studyId ID of the study
     * @return
     */
    public Optional<Study> findStudyByStudyId(String studyId) {
        return studyRepository.findById(studyId);
    }

    /**
     * Find studies by owner
     * @param owner ID of the owner
     * @return
     */
    public List<Study> findStudyByOwner(String owner) {
        return studyRepository.findByOwner(owner);
    }

    /**
     * Save a study
     * @param study study to be saved
     * @return
     */
    public Study saveStudy(Study study) {
        return studyRepository.save(study);
    }

    /**
     * Update a study
     * @param studyId ID of the study
     * @param updatedStudy study to be updated
     * @return
     */
    public Optional<Study> updateStudy(String studyId, Study updatedStudy) {
        Optional<Study> oldStudy = studyRepository.findById(studyId);
        if (oldStudy.isPresent()) {
            Study study = oldStudy.get();
            study.setName(updatedStudy.getName());
            study.setDescription(updatedStudy.getDescription());
            study.setObjectives(updatedStudy.getObjectives());
            study.setEthics(updatedStudy.getEthics());
            study.setOwner(updatedStudy.getOwner());
            Study savedStudy = studyRepository.save(study);
            return Optional.of(savedStudy);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a study
     * @param studyId ID of study to be deleted
     * @return
     */
    public Optional<Study> deleteStudy(String studyId) {
        Optional<Study> existingStudy = studyRepository.findById(studyId);
        if (existingStudy.isPresent()) {
            studyRepository.delete(existingStudy.get());
            return existingStudy;
        } else {
            return Optional.empty();
        }
    }


    /**
     * Find a Study by datasetId
     * @param datasetId ID of the Dataset
     * @return
     */
    public Study findRelatedStudyByDatasetId(String datasetId) {
        return studyRepository.findByDatasetId(datasetId);
    }
}
