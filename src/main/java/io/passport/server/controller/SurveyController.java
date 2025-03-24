package io.passport.server.controller;

import io.passport.server.model.*;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.RoleCheckerService;
import io.passport.server.service.SurveyService;
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

    private final String relationName = "Survey";
    private final SurveyService surveyService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    private final List<Role> allowedRoles = List.of(Role.STUDY_OWNER, Role.SURVEY_MANAGER);

    @Autowired
    public SurveyController(SurveyService surveyService,
                            RoleCheckerService roleCheckerService,
                            AuditLogBookService auditLogBookService) {
        this.surveyService = surveyService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Retrieves a survey by its ID.
     *
     * @param surveyId  ID of the survey
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Survey or 403 if unauthorized
     */
    @GetMapping("/{surveyId}")
    public ResponseEntity<?> getSurveyById(@PathVariable("surveyId") String surveyId,
                                           @RequestParam String studyId,
                                           @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Optional<Survey> surveyOpt = this.surveyService.findSurveyById(surveyId);
            return surveyOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error retrieving Survey: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Retrieves surveys by studyId.
     *
     * @param studyId   ID of the study
     * @param principal Jwt principal containing user info
     * @return List of surveys
     */
    @GetMapping
    public ResponseEntity<List<Survey>> getSurveys(@RequestParam(value = "studyId") String studyId,
                                                   @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Survey> surveys = surveyService.findSurveysByStudyId(studyId);
        return ResponseEntity.ok(surveys);
    }

    /**
     * Creates a new Survey.
     *
     * @param survey    Survey object to create
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Created Survey or BAD_REQUEST
     */
    @PostMapping
    public ResponseEntity<?> createSurvey(@RequestBody Survey survey,
                                          @RequestParam String studyId,
                                          @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Survey savedSurvey = this.surveyService.saveSurvey(survey);

            if (savedSurvey.getSurveyId() != null) {
                String recordId = savedSurvey.getSurveyId();
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.CREATE,
                        relationName,
                        recordId,
                        savedSurvey
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(savedSurvey);

        } catch (Exception e) {
            log.error("Error creating Survey: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Updates an existing Survey.
     *
     * @param surveyId      ID of the survey to update
     * @param studyId       ID of the study for authorization
     * @param updatedSurvey Updated survey details
     * @param principal     Jwt principal containing user info
     * @return Updated Survey or NOT_FOUND
     */
    @PutMapping("/{surveyId}")
    public ResponseEntity<?> updateSurvey(@PathVariable String surveyId,
                                          @RequestParam String studyId,
                                          @RequestBody Survey updatedSurvey,
                                          @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Optional<Survey> savedSurveyOpt = this.surveyService.updateSurvey(surveyId, updatedSurvey);
            if (savedSurveyOpt.isPresent()) {
                Survey savedSurvey = savedSurveyOpt.get();
                String recordId = savedSurvey.getSurveyId();
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.UPDATE,
                        relationName,
                        recordId,
                        savedSurvey
                );
                return ResponseEntity.ok(savedSurvey);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating Survey: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Deletes a Survey by surveyId.
     *
     * @param surveyId  ID of the Survey to delete
     * @param studyId   ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return No content if deleted, NOT_FOUND otherwise
     */
    @DeleteMapping("/{surveyId}")
    public ResponseEntity<?> deleteSurvey(@PathVariable String surveyId,
                                          @RequestParam String studyId,
                                          @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Optional<Survey> deletedSurvey = this.surveyService.deleteSurvey(surveyId);
            if (deletedSurvey.isPresent()) {
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.DELETE,
                        relationName,
                        surveyId,
                        deletedSurvey.get()
                );
                return ResponseEntity.status(HttpStatus.OK).body(deletedSurvey.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting Survey: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
