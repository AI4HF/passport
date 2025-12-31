package io.passport.server.service;

import io.passport.server.model.Role;
import io.passport.server.model.Survey;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.SurveyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for survey management.
 */
@Service
public class SurveyService {

    /**
     * Survey repo access for database management.
     */
    private final SurveyRepository surveyRepository;
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public SurveyService(SurveyRepository surveyRepository,
                         RoleCheckerService roleCheckerService) {
        this.surveyRepository = surveyRepository;
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
        List<Survey> affectedSurveys;

        switch (sourceResourceType) {
            case "Study":
                affectedSurveys = surveyRepository.findAllByStudyId(sourceResourceId);
                break;
            default:
                return new ValidationResult(true, "");
        }

        if (affectedSurveys.isEmpty()) {
            return new ValidationResult(true, "");
        }

        boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                studyId,
                principal,
                List.of(Role.SURVEY_MANAGER)
        );

        if (!hasPermission) {
            return new ValidationResult(false, "Survey");
        }

        return new ValidationResult(true, "Survey");
    }

    /**
     * Find all surveys.
     * @return
     */
    public List<Survey> findAllSurveys() { return surveyRepository.findAll(); }

    /**
     * Find a survey by surveyId
     * @param surveyId ID of the survey
     * @return
     */
    public Optional<Survey> findSurveyById(String surveyId) {
        return surveyRepository.findById(surveyId);
    }

    /**
     * Find surveys by studyId
     * @param studyId ID of the study
     * @return
     */
    public List<Survey> findSurveysByStudyId(String studyId) {
        return surveyRepository.findAllByStudyId(studyId);
    }

    /**
     * Save a survey
     * @param survey survey to be saved
     * @return
     */
    public Survey saveSurvey(Survey survey) {
        return surveyRepository.save(survey);
    }

    /**
     * Update a survey
     * @param surveyId ID of the survey
     * @param updatedSurvey survey to be updated
     * @return
     */
    public Optional<Survey> updateSurvey(String surveyId, Survey updatedSurvey) {
        Optional<Survey> oldSurvey = surveyRepository.findById(surveyId);
        if(oldSurvey.isPresent()) {
            Survey survey = oldSurvey.get();
            survey.setStudyId(updatedSurvey.getStudyId());
            survey.setQuestion(updatedSurvey.getQuestion());
            survey.setAnswer(updatedSurvey.getAnswer());
            survey.setCategory(updatedSurvey.getCategory());
            Survey savedSurvey = surveyRepository.save(survey);
            return Optional.of(savedSurvey);
        }else{
            return Optional.empty();
        }
    }

    /**
     * Delete a survey
     * @param surveyId ID of survey to be deleted
     * @return
     */
    public Optional<Survey> deleteSurvey(String surveyId) {
        Optional<Survey> existingSurvey = surveyRepository.findById(surveyId);
        if (existingSurvey.isPresent()) {
            surveyRepository.delete(existingSurvey.get());
            return existingSurvey;
        } else {
            return Optional.empty();
        }
    }

}
