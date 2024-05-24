package io.passport.server.controller;

import io.passport.server.model.Survey;
import io.passport.server.repository.SurveyRepository;
import io.passport.server.service.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Class which stores the generated HTTP requests related to survey operations.
 */
@RestController
@RequestMapping("/surveys")
public class SurveyController {
    /**
     * Survey repo access for database management.
     */
    private final SurveyRepository surveyRepository;

    @Autowired
    public SurveyController(SurveyRepository surveyRepository) {
        this.surveyRepository = surveyRepository;
    }

    @GetMapping("/")
    public ResponseEntity<List<Survey>> getAllSurveys(
            @RequestParam Long studyId,
            @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, 10);
        Page<Survey> surveyPage = surveyRepository.findByStudyId(studyId, pageable);

        List<Survey> surveyList = surveyPage.getContent();
        long totalCount = surveyPage.getTotalElements();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(surveyList);
    }

    /**
     * Create Survey.
     * @param survey Survey model instance to be created.
     * @return
     */
    @PostMapping("/")
    public ResponseEntity<Survey> createSurvey(@RequestBody Survey survey) {
        Survey savedSurvey = surveyRepository.save(survey);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSurvey);
    }

    /**
     * Update Survey.
     * @param surveyId ID of the survey that is to be updated.
     * @param survey Survey model instance with updated details.
     * @return
     */
    @PutMapping("/{surveyId}")
    public ResponseEntity<Survey> updateSurvey(
            @PathVariable Long surveyId,
            @RequestBody Survey survey) {
        return surveyRepository.findById(surveyId)
                .map(existingSurvey -> {
                    try {
                        Utils.copyNonNullProperties(survey, existingSurvey);
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    Survey updatedSurvey = surveyRepository.save(existingSurvey);
                    return ResponseEntity.ok(updatedSurvey);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Delete by Survey ID.
     * @param surveyId ID of the survey that is to be deleted.
     * @return
     */
    @DeleteMapping("/{surveyId}")
    public ResponseEntity<Object> deleteSurvey(@PathVariable Long surveyId) {
        return surveyRepository.findById(surveyId)
                .map(survey -> {
                    surveyRepository.delete(survey);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
