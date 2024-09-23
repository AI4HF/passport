package io.passport.server.controller;

import io.passport.server.model.Role;
import io.passport.server.model.Survey;
import io.passport.server.service.RoleCheckerService;
import io.passport.server.service.SurveyService;
import org.keycloak.KeycloakPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    /**
     * Role checker service for authorization
     */
    private final RoleCheckerService roleCheckerService;

    /**
     * List of authorized roles for this endpoint
     */
    private final List<Role> allowedRoles = List.of(Role.STUDY_OWNER, Role.SURVEY_MANAGER);

    @Autowired
    public SurveyController(SurveyService surveyService, RoleCheckerService roleCheckerService) {
        this.surveyService = surveyService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read survey by surveyId
     * @param surveyId ID of the survey.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping("/{surveyId}")
    public ResponseEntity<?> getSurveyById(@PathVariable("surveyId") Long surveyId,
                                           @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Survey> survey = this.surveyService.findSurveyById(surveyId);

        if(survey.isPresent()) {
            return ResponseEntity.ok(survey.get());
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Read surveys, if studyId is provided, filter by studyId; otherwise, return all surveys.
     * @param studyId Optional ID of the study.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<Survey>> getSurveys(@RequestParam(value = "studyId", required = false) Long studyId,
                                                   @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Survey> surveys;

        if (studyId != null) {
            surveys = surveyService.findSurveysByStudyId(studyId);
        } else {
            surveys = surveyService.findAllSurveys();
        }

        return ResponseEntity.ok(surveys);
    }

    /**
     * Create Survey.
     * @param survey Survey model instance to be created.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createSurvey(@RequestBody Survey survey,
                                          @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> lesserAllowedRoles = List.of(Role.SURVEY_MANAGER);
        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, lesserAllowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

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
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PutMapping("/{surveyId}")
    public ResponseEntity<?> updateSurvey(@PathVariable Long surveyId,
                                          @RequestBody Survey updatedSurvey,
                                          @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> lesserAllowedRoles = List.of(Role.SURVEY_MANAGER);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, lesserAllowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

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
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @DeleteMapping("/{surveyId}")
    public ResponseEntity<?> deleteSurvey(@PathVariable Long surveyId,
                                          @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> lesserAllowedRoles = List.of(Role.SURVEY_MANAGER);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, lesserAllowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

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
