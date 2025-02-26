package io.passport.server.service;

import io.passport.server.model.Survey;
import io.passport.server.repository.SurveyRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public SurveyService(SurveyRepository surveyRepository) {
        this.surveyRepository = surveyRepository;
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
