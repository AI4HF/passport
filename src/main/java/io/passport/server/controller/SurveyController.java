package io.passport.server.controller;

import io.passport.server.model.Survey;
import io.passport.server.service.SurveyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Class which stores the generated HTTP requests related to survey operations.
 */
@RestController
@RequestMapping("/survey")
public class SurveyController {

    private static final Logger log = LoggerFactory.getLogger(SurveyController.class);

    /**
     * Survey service for survey management
     */
    private final SurveyService surveyService;

    @Autowired
    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    /**
     * Read all surveys
     * @return
     */
    @GetMapping("/all")
    public ResponseEntity<List<Survey>> getAllSurveys() {

        List<Survey> surveys = this.surveyService.findAllSurveys();

        return ResponseEntity.ok(surveys);
    }

    /**
     * Read survey by surveyId
     * @param surveyId ID of the survey.
     * @return
     */
    @GetMapping("/{surveyId}")
    public ResponseEntity<?> getSurveyById(@PathVariable("surveyId") Long surveyId) {

        Optional<Survey> survey = this.surveyService.findSurveyById(surveyId);

        if(survey.isPresent()) {
            return ResponseEntity.ok(survey.get());
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Read surveys by surveyId
     * @param studyId ID of the study.
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<Survey>> getSurveysByStudyId(@RequestParam("studyId") Long studyId) {

        List<Survey> survey = this.surveyService.findSurveysByStudyId(studyId);

        return ResponseEntity.ok(survey);
    }

    /**
     * Create Survey.
     * @param survey Survey model instance to be created.
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createSurvey(@RequestBody Survey survey) {
        try{
            Survey savedSurvey = this.surveyService.saveSurvey(survey);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedSurvey);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Survey.
     * @param surveyId ID of the survey that is to be updated.
     * @param updatedSurvey model instance with updated details.
     * @return
     */
    @PutMapping("/{surveyId}")
    public ResponseEntity<?> updateSurvey(@PathVariable Long surveyId, @RequestBody Survey updatedSurvey) {
        try{
            Optional<Survey> savedSurvey = this.surveyService.updateSurvey(surveyId, updatedSurvey);
            if(savedSurvey.isPresent()) {
                return ResponseEntity.ok(savedSurvey.get());
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by survey ID.
     * @param surveyId ID of the survey that is to be deleted.
     * @return
     */
    @DeleteMapping("/{surveyId}")
    public ResponseEntity<?> deleteSurvey(@PathVariable Long surveyId) {
        try{
            boolean isDeleted = this.surveyService.deleteSurvey(surveyId);
            if(isDeleted) {
                return ResponseEntity.noContent().build();
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
