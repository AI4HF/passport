package io.passport.server.controller;

import io.passport.server.model.Role;
import io.passport.server.model.Survey;
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

    private final SurveyService surveyService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService; // <-- NEW

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
    public ResponseEntity<?> getSurveyById(@PathVariable("surveyId") Long surveyId,
                                           @RequestParam Long studyId,
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
    public ResponseEntity<List<Survey>> getSurveys(@RequestParam(value = "studyId") Long studyId,
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
                                          @RequestParam Long studyId,
                                          @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Survey savedSurvey = this.surveyService.saveSurvey(survey);

            // Audit log
            if (savedSurvey.getSurveyId() != null) {
                String recordId = savedSurvey.getSurveyId().toString();
                String description = "Creation of Survey " + recordId;
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim("preferred_username"),
                        studyId,
                        "CREATE",
                        "Survey",
                        recordId,
                        savedSurvey,
                        description
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
    public ResponseEntity<?> updateSurvey(@PathVariable Long surveyId,
                                          @RequestParam Long studyId,
                                          @RequestBody Survey updatedSurvey,
                                          @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Optional<Survey> savedSurveyOpt = this.surveyService.updateSurvey(surveyId, updatedSurvey);
            if (savedSurveyOpt.isPresent()) {
                Survey savedSurvey = savedSurveyOpt.get();
                String recordId = savedSurvey.getSurveyId().toString();
                String description = "Update of Survey " + recordId;
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim("preferred_username"),
                        studyId,
                        "UPDATE",
                        "Survey",
                        recordId,
                        savedSurvey,
                        description
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
    public ResponseEntity<?> deleteSurvey(@PathVariable Long surveyId,
                                          @RequestParam Long studyId,
                                          @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            boolean isDeleted = this.surveyService.deleteSurvey(surveyId);
            if (isDeleted) {
                String description = "Deletion of Survey " + surveyId;
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim("preferred_username"),
                        studyId,
                        "DELETE",
                        "Survey",
                        surveyId.toString(),
                        null,
                        description
                );
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting Survey: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
