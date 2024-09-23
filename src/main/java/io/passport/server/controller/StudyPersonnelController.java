package io.passport.server.controller;

import io.passport.server.model.Personnel;
import io.passport.server.model.Role;
import io.passport.server.model.Study;
import io.passport.server.service.RoleCheckerService;
import io.passport.server.service.StudyPersonnelService;
import org.keycloak.KeycloakPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Class which stores the generated HTTP requests related to StudyPersonnel operations.
 */
@RestController
@RequestMapping("/studyPersonnel")
public class StudyPersonnelController {

    private static final Logger log = LoggerFactory.getLogger(StudyPersonnelController.class);

    /**
     * StudyPersonnel service for studyPersonnel management
     */
    private final StudyPersonnelService studyPersonnelService;

    /**
     * Role checker service for authorization
     */
    private final RoleCheckerService roleCheckerService;

    /**
     * List of authorized roles for this endpoint
     */
    private final List<Role> allowedRoles = List.of(Role.STUDY_OWNER, Role.DATA_SCIENTIST, Role.DATA_ENGINEER, Role.SURVEY_MANAGER, Role.QUALITY_ASSURANCE_SPECIALIST);

    @Autowired
    public StudyPersonnelController(StudyPersonnelService studyPersonnelService, RoleCheckerService roleCheckerService) {
        this.studyPersonnelService = studyPersonnelService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Get all personnel related to a study and an organization.
     * @param studyId ID of the study.
     * @param organizationId ID of the organization
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping("/personnel")
    public ResponseEntity<?> getPersonnelByStudyId(@RequestParam Long studyId,
                                                   @RequestParam Long organizationId,
                                                   @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> lesserAllowedRoles = List.of(Role.STUDY_OWNER);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, lesserAllowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<Personnel> personnel = this.studyPersonnelService.findPersonnelByStudyIdAndOrganizationId(studyId, organizationId);
            return ResponseEntity.ok(personnel);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Get all studies related to a personnel.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping("/studies")
    public ResponseEntity<?> getPersonnelByStudyId(@AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            String personnelId = this.roleCheckerService.getPersonnelId(principal);
            List<Study> studies = this.studyPersonnelService.findStudiesByPersonnelId(personnelId);
            return ResponseEntity.ok(studies);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Clear all old StudyPersonnel entries related to both the study and the organization then create new ones. Return updated personnel list.
     * @param studyId ID of the study.
     * @param organizationId ID of the organization.
     * @param personnel List of personnel to be used in StudyPersonnel entries
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PostMapping("/personnel")
    public ResponseEntity<?> createStudyPersonnelEntries(@RequestParam Long studyId,
                                                         @RequestParam Long organizationId,
                                                         @RequestBody List<Personnel> personnel,
                                                         @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> lesserAllowedRoles = List.of(Role.STUDY_OWNER);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, lesserAllowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            this.studyPersonnelService.createStudyPersonnelEntries(studyId, organizationId, personnel);
            List<Personnel> updatedPersonnel = this.studyPersonnelService.findPersonnelByStudyIdAndOrganizationId(studyId, organizationId);
            return ResponseEntity.ok(updatedPersonnel);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
