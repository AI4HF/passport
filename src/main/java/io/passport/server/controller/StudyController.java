package io.passport.server.controller;

import io.passport.server.model.Role;
import io.passport.server.model.Study;
import io.passport.server.service.RoleCheckerService;
import io.passport.server.service.StudyService;
import org.keycloak.KeycloakPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Class which stores the generated HTTP requests related to study operations.
 */
@RestController
@RequestMapping("/study")
public class StudyController {
    private static final Logger log = LoggerFactory.getLogger(StudyController.class);
    /**
     * Study service for study management
     */
    private final StudyService studyService;

    /**
     * Role checker service for authorization
     */
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public StudyController(StudyService studyService, RoleCheckerService roleCheckerService) {
        this.studyService = studyService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read all studies
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<Study>> getAllStudies(@AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> allowedRoles = List.of(Role.STUDY_OWNER, Role.SURVEY_MANAGER, Role.DATA_SCIENTIST);
        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Study> studies = this.studyService.getAllStudies();

        long totalCount = studies.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(studies);
    }

    /**
     * Read a study by id
     * @param studyId ID of the study
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping("/{studyId}")
    public ResponseEntity<?> getStudy(@PathVariable Long studyId, @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> allowedRoles = List.of(Role.STUDY_OWNER, Role.SURVEY_MANAGER, Role.DATA_SCIENTIST);
        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Study> study = this.studyService.findStudyByStudyId(studyId);

        if(study.isPresent()) {
            return ResponseEntity.ok().body(study.get());
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create Study.
     * @param study Study model instance to be created.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createStudy(@RequestBody Study study, @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.STUDY_OWNER);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Study savedStudy = this.studyService.saveStudy(study);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedStudy);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Study.
     * @param studyId ID of the study that is to be updated.
     * @param updatedStudy Study model instance with updated details.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PutMapping("/{studyId}")
    public ResponseEntity<?> updateStudy(@PathVariable Long studyId, @RequestBody Study updatedStudy, @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.STUDY_OWNER);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<Study> savedStudy = this.studyService.updateStudy(studyId, updatedStudy);
            if(savedStudy.isPresent()) {
                return ResponseEntity.ok().body(savedStudy);
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by Study ID.
     * @param studyId ID of the study that is to be deleted.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @DeleteMapping("/{studyId}")
    public ResponseEntity<?> deleteStudy(@PathVariable Long studyId, @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.STUDY_OWNER);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.studyService.deleteStudy(studyId);
            if(isDeleted) {
                return ResponseEntity.noContent().build();
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
