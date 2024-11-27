package io.passport.server.controller;

import io.passport.server.model.*;
import io.passport.server.service.RoleCheckerService;
import io.passport.server.service.StudyOrganizationService;
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
 * Class which stores the generated HTTP requests related to StudyOrganization operations.
 */
@RestController
@RequestMapping("/studyOrganization")
public class StudyOrganizationController {

    private static final Logger log = LoggerFactory.getLogger(StudyOrganizationController.class);

    private final StudyOrganizationService studyOrganizationService;
    private final RoleCheckerService roleCheckerService;

    private final List<Role> allowedRoles = List.of(Role.STUDY_OWNER);

    @Autowired
    public StudyOrganizationController(StudyOrganizationService studyOrganizationService, RoleCheckerService roleCheckerService) {
        this.studyOrganizationService = studyOrganizationService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Get a studyOrganization by studyOrganizationId.
     * @param studyId ID of the study.
     * @param organizationId ID of the organization.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping()
    public ResponseEntity<?> getStudyOrganizationByStudyOrganizationId(@RequestParam Long studyId,
                                                                       @RequestParam Long organizationId,
                                                                       @AuthenticationPrincipal Jwt principal) {
        // Check authorization using studyId
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            StudyOrganizationId studyOrganizationId = new StudyOrganizationId(organizationId, studyId);
            Optional<StudyOrganization> studyOrganization = this.studyOrganizationService.findStudyOrganizationById(studyOrganizationId);
            if (studyOrganization.isPresent()) {
                StudyOrganizationDTO studyOrganizationDTO = new StudyOrganizationDTO(studyOrganization.get());
                return ResponseEntity.ok(studyOrganizationDTO);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Get all organizations related to a study.
     * @param studyId ID of the study.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping("/organizations")
    public ResponseEntity<?> getOrganizationsByStudyId(@RequestParam Long studyId,
                                                       @AuthenticationPrincipal Jwt principal) {
        // Check authorization using studyId
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<Organization> organizations = this.studyOrganizationService.findOrganizationsByStudyId(studyId);
            return ResponseEntity.ok(organizations);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Get all studies related to an organization.
     * @param organizationId ID of the organization.
     * @param studyId ID of the study
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping("/studies")
    public ResponseEntity<?> getStudiesByOrganizationId(@RequestParam Long organizationId,
                                                        @RequestParam Long studyId,
                                                        @AuthenticationPrincipal Jwt principal) {
        // Check authorization using studyId
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<Study> studies = this.studyOrganizationService.findStudiesByOrganizationId(organizationId);
            return ResponseEntity.ok(studies);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Create a study organization.
     * @param studyOrganizationDTO studyOrganization object to be created.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createStudyOrganization(@RequestBody StudyOrganizationDTO studyOrganizationDTO,
                                                     @AuthenticationPrincipal Jwt principal) {
        Long studyId = studyOrganizationDTO.getStudyId();

        // Check authorization using studyId
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            StudyOrganization studyOrganization = new StudyOrganization(studyOrganizationDTO);
            StudyOrganization savedStudyOrganization = this.studyOrganizationService.createStudyOrganizationEntries(studyOrganization);
            StudyOrganizationDTO responseStudyOrganizationDTO = new StudyOrganizationDTO(savedStudyOrganization);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseStudyOrganizationDTO);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update study organization.
     * @param studyId ID of the study
     * @param organizationId ID of the organization
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PutMapping()
    public ResponseEntity<?> updateStudyOrganization(@RequestParam Long studyId,
                                                     @RequestParam Long organizationId,
                                                     @RequestBody StudyOrganizationDTO updatedStudyOrganizationDTO,
                                                     @AuthenticationPrincipal Jwt principal) {
        // Check authorization using studyId
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            StudyOrganization studyOrganization = new StudyOrganization(updatedStudyOrganizationDTO);
            StudyOrganizationId studyOrganizationId = new StudyOrganizationId(organizationId, studyId);
            Optional<StudyOrganization> savedStudyOrganization = this.studyOrganizationService.updateStudyOrganization(studyOrganizationId, studyOrganization);
            if (savedStudyOrganization.isPresent()) {
                StudyOrganizationDTO savedStudyOrganizationDTO = new StudyOrganizationDTO(savedStudyOrganization.get());
                return ResponseEntity.ok().body(savedStudyOrganizationDTO);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete a study organization by studyOrganizationId.
     * @param studyId ID of the study.
     * @param organizationId ID of the organization.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @DeleteMapping()
    public ResponseEntity<?> deleteStudyOrganization(@RequestParam Long studyId,
                                                     @RequestParam Long organizationId,
                                                     @AuthenticationPrincipal Jwt principal) {
        // Check authorization using studyId
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            StudyOrganizationId studyOrganizationId = new StudyOrganizationId(organizationId, studyId);
            boolean isDeleted = this.studyOrganizationService.deleteStudyOrganization(studyOrganizationId);
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

