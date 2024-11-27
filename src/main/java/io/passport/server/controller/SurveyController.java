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
import org.springframework.security.oauth2.jwt.Jwt;
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

    private final SurveyService surveyService;
    private final RoleCheckerService roleCheckerService;

    private final List<Role> allowedRoles = List.of(Role.STUDY_OWNER, Role.SURVEY_MANAGER);

    @Autowired
    public SurveyController(SurveyService surveyService, RoleCheckerService roleCheckerService) {
        this.surveyService = surveyService;
        this.roleCheckerService = roleCheckerService;
    }

    @GetMapping("/{surveyId}")
    public ResponseEntity<?> getSurveyById(@PathVariable("surveyId") Long surveyId,
                                           @RequestParam Long studyId,
                                           @AuthenticationPrincipal Jwt principal) {
        // Check authorization using studyId
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Optional<Survey> survey = this.surveyService.findSurveyById(surveyId);
            return survey.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping()
    public ResponseEntity<List<Survey>> getSurveys(@RequestParam(value = "studyId") Long studyId,
                                                   @AuthenticationPrincipal Jwt principal) {
        // Check authorization using studyId
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Survey> surveys = surveyService.findSurveysByStudyId(studyId);
        return ResponseEntity.ok(surveys);

    }

    @PostMapping()
    public ResponseEntity<?> createSurvey(@RequestBody Survey survey,
                                          @RequestParam Long studyId,
                                          @AuthenticationPrincipal Jwt principal) {
        // Check authorization using studyId
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Survey savedSurvey = this.surveyService.saveSurvey(survey);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedSurvey);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{surveyId}")
    public ResponseEntity<?> updateSurvey(@PathVariable Long surveyId,
                                          @RequestParam Long studyId,
                                          @RequestBody Survey updatedSurvey,
                                          @AuthenticationPrincipal Jwt principal) {
        // Check authorization using studyId
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Optional<Survey> savedSurvey = this.surveyService.updateSurvey(surveyId, updatedSurvey);
            return savedSurvey.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{surveyId}")
    public ResponseEntity<?> deleteSurvey(@PathVariable Long surveyId,
                                          @RequestParam Long studyId,
                                          @AuthenticationPrincipal Jwt principal) {
        // Check authorization using studyId
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            boolean isDeleted = this.surveyService.deleteSurvey(surveyId);
            if (isDeleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
