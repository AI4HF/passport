package io.passport.server.controller;

import io.passport.server.model.*;
import io.passport.server.service.RoleCheckerService;
import io.passport.server.service.StudyPersonnelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class which stores the generated HTTP requests related to StudyPersonnel operations.
 */
@RestController
@RequestMapping("/studyPersonnel")
public class StudyPersonnelController {

    private static final Logger log = LoggerFactory.getLogger(StudyPersonnelController.class);

    private final StudyPersonnelService studyPersonnelService;
    private final RoleCheckerService roleCheckerService;
    private final List<Role> allowedRoles = List.of(Role.STUDY_OWNER, Role.DATA_SCIENTIST, Role.DATA_ENGINEER, Role.SURVEY_MANAGER, Role.QUALITY_ASSURANCE_SPECIALIST);

    @Autowired
    public StudyPersonnelController(StudyPersonnelService studyPersonnelService, RoleCheckerService roleCheckerService) {
        this.studyPersonnelService = studyPersonnelService;
        this.roleCheckerService = roleCheckerService;
    }

    @GetMapping("/roles")
    public ResponseEntity<?> getPersonnelRolesByStudyAndOrganization(@RequestParam String studyId,
                                                                     @RequestParam String organizationId,
                                                                     @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedToViewStudy(studyId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Map<String, List<String>> personnelRoles =
                    studyPersonnelService.getPersonnelRolesByStudyAndOrganization(studyId, organizationId);
            return ResponseEntity.ok(personnelRoles);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    /**
     * Get all personnel related to a study and an organization.
     * @param studyId ID of the study.
     * @param organizationId ID of the organization.
     * @param principal KeycloakPrincipal object that holds access token.
     * @return ResponseEntity with the personnel data.
     */
    @GetMapping("/personnel")
    public ResponseEntity<?> getPersonnelByStudyId(@RequestParam String studyId,
                                                   @RequestParam String organizationId,
                                                   @AuthenticationPrincipal Jwt principal) {
        // Check user authorization for the given study
        if (!this.roleCheckerService.isUserAuthorizedToViewStudy(studyId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<Personnel> personnel = this.studyPersonnelService.findPersonnelByStudyIdAndOrganizationId(studyId, organizationId);
            return ResponseEntity.ok(personnel);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Get all studies related to a personnel.
     * @param principal KeycloakPrincipal object that holds access token.
     * @return ResponseEntity with the studies data.
     */
    @GetMapping("/studies")
    public ResponseEntity<?> getPersonnelByStudyId(@AuthenticationPrincipal Jwt principal) {
        // Check user authorization for any role in the allowedRoles list
        if (!this.roleCheckerService.hasAnyRole(principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            String personnelId = principal.getSubject();
            List<Study> studies = this.studyPersonnelService.findStudiesByPersonnelId(personnelId);
            return ResponseEntity.ok(studies);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/personnel")
    public ResponseEntity<?> createStudyPersonnelEntries(
            @RequestParam String studyId,
            @RequestParam String organizationId,
            @RequestBody List<PersonnelRoleDTO> personnelRoleList,
            @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, List.of(Role.STUDY_OWNER))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Map<String, List<String>> personnelRoleMap = personnelRoleList.stream()
                    .collect(Collectors.toMap(
                            PersonnelRoleDTO::getPersonId,
                            PersonnelRoleDTO::getRoles
                    ));

            this.studyPersonnelService.createStudyPersonnelEntries(studyId, organizationId, personnelRoleMap);

            List<Personnel> updatedPersonnel = this.studyPersonnelService.findPersonnelByStudyIdAndOrganizationId(studyId, organizationId);
            return ResponseEntity.ok(updatedPersonnel);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    /**
     * Fetch all StudyPersonnel entries for a given personnel by their personId.
     * @param personId ID of the personnel.
     * @param principal KeycloakPrincipal object that holds access token.
     * @return ResponseEntity containing the list of StudyPersonnel entries.
     */
    @GetMapping("")
    public ResponseEntity<?> getStudyPersonnelByPersonId(@RequestParam String personId,
                                                         @AuthenticationPrincipal Jwt principal) {

        try {
            List<StudyPersonnel> studyPersonnelList = this.studyPersonnelService.findStudyPersonnelByPersonId(personId);
            return ResponseEntity.ok(studyPersonnelList);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
