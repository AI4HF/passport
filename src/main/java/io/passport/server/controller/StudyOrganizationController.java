package io.passport.server.controller;

import io.passport.server.model.*;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.RoleCheckerService;
import io.passport.server.service.StudyOrganizationService;
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

    private final String relationName = "Study Organization";
    private final StudyOrganizationService studyOrganizationService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    private final List<Role> allowedRoles = List.of(Role.STUDY_OWNER);

    @Autowired
    public StudyOrganizationController(StudyOrganizationService studyOrganizationService,
                                       RoleCheckerService roleCheckerService,
                                       AuditLogBookService auditLogBookService) {
        this.studyOrganizationService = studyOrganizationService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Get a studyOrganization by (studyId, organizationId).
     *
     * @param studyId        ID of the study.
     * @param organizationId ID of the organization.
     * @param principal      Jwt principal containing user info
     * @return StudyOrganizationDTO or NOT_FOUND
     */
    @GetMapping
    public ResponseEntity<?> getStudyOrganizationByStudyOrganizationId(@RequestParam Long studyId,
                                                                       @RequestParam Long organizationId,
                                                                       @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            StudyOrganizationId id = new StudyOrganizationId(organizationId, studyId);
            Optional<StudyOrganization> soOpt = this.studyOrganizationService.findStudyOrganizationById(id);
            if (soOpt.isPresent()) {
                StudyOrganizationDTO dto = new StudyOrganizationDTO(soOpt.get());
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error retrieving StudyOrganization: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Get all organizations related to a study.
     *
     * @param studyId   ID of the study.
     * @param principal Jwt principal containing user info
     * @return List of Organization or BAD_REQUEST
     */
    @GetMapping("/organizations")
    public ResponseEntity<?> getOrganizationsByStudyId(@RequestParam Long studyId,
                                                       @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<Organization> organizations = this.studyOrganizationService.findOrganizationsByStudyId(studyId);
            return ResponseEntity.ok(organizations);
        } catch (Exception e) {
            log.error("Error retrieving organizations by studyId: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Get all studies related to an organization.
     *
     * @param organizationId ID of the organization.
     * @param studyId        ID of the study (for authorization)
     * @param principal      Jwt principal containing user info
     * @return List of Study or BAD_REQUEST
     */
    @GetMapping("/studies")
    public ResponseEntity<?> getStudiesByOrganizationId(@RequestParam Long organizationId,
                                                        @RequestParam Long studyId,
                                                        @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<Study> studies = this.studyOrganizationService.findStudiesByOrganizationId(organizationId);
            return ResponseEntity.ok(studies);
        } catch (Exception e) {
            log.error("Error retrieving studies by organizationId: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Create a study organization.
     *
     * @param studyOrganizationDTO data to create
     * @param principal            Jwt principal containing user info
     * @return Created StudyOrganizationDTO
     */
    @PostMapping
    public ResponseEntity<?> createStudyOrganization(@RequestBody StudyOrganizationDTO studyOrganizationDTO,
                                                     @AuthenticationPrincipal Jwt principal) {
        Long studyId = studyOrganizationDTO.getStudyId();
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            StudyOrganization entity = new StudyOrganization(studyOrganizationDTO);
            StudyOrganization saved = this.studyOrganizationService.createStudyOrganizationEntries(entity);

            StudyOrganizationDTO responseDTO = new StudyOrganizationDTO(saved);

            if (saved.getId() != null) {
                Long orgId = saved.getId().getOrganizationId();
                Long stdId = saved.getId().getStudyId();
                String compositeId = "(" + stdId + ", " + orgId + ")";
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.CREATE,
                        relationName,
                        compositeId,
                        saved
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);

        } catch (Exception e) {
            log.error("Error creating StudyOrganization: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update a study organization.
     *
     * @param studyId                     ID of the study
     * @param organizationId              ID of the organization
     * @param updatedStudyOrganizationDTO updated data
     * @param principal                   Jwt principal containing user info
     * @return Updated StudyOrganizationDTO or NOT_FOUND
     */
    @PutMapping
    public ResponseEntity<?> updateStudyOrganization(@RequestParam Long studyId,
                                                     @RequestParam Long organizationId,
                                                     @RequestBody StudyOrganizationDTO updatedStudyOrganizationDTO,
                                                     @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            StudyOrganization entity = new StudyOrganization(updatedStudyOrganizationDTO);
            StudyOrganizationId id = new StudyOrganizationId(organizationId, studyId);
            Optional<StudyOrganization> savedOpt = this.studyOrganizationService.updateStudyOrganization(id, entity);

            if (savedOpt.isPresent()) {
                StudyOrganization saved = savedOpt.get();
                StudyOrganizationDTO responseDTO = new StudyOrganizationDTO(saved);
                Long orgId = saved.getId().getOrganizationId();
                Long stdId = saved.getId().getStudyId();
                String compositeId = "(" + stdId + ", " + orgId + ")";
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.UPDATE,
                        relationName,
                        compositeId,
                        saved
                );
                return ResponseEntity.ok().body(responseDTO);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error updating StudyOrganization: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete a study organization by (studyId, organizationId).
     *
     * @param studyId        ID of the study.
     * @param organizationId ID of the organization.
     * @param principal      Jwt principal containing user info
     * @return No content or NOT_FOUND
     */
    @DeleteMapping
    public ResponseEntity<?> deleteStudyOrganization(@RequestParam Long studyId,
                                                     @RequestParam Long organizationId,
                                                     @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            StudyOrganizationId id = new StudyOrganizationId(organizationId, studyId);
            Optional<StudyOrganization> deletedStudyOrganization = this.studyOrganizationService.deleteStudyOrganization(id);
            if (deletedStudyOrganization.isPresent()) {
                String compositeId = "(" + studyId + ", " + organizationId + ")";
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.DELETE,
                        relationName,
                        compositeId,
                        deletedStudyOrganization.get()
                );
                return ResponseEntity.status(HttpStatus.OK).body(deletedStudyOrganization.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting StudyOrganization: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

