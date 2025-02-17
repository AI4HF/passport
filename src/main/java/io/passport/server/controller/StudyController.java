package io.passport.server.controller;

import io.passport.server.model.Role;
import io.passport.server.model.Study;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.KeycloakService;
import io.passport.server.service.RoleCheckerService;
import io.passport.server.service.StudyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller class for managing HTTP requests related to study operations.
 */
@RestController
@RequestMapping("/study")
public class StudyController {

    private static final Logger log = LoggerFactory.getLogger(StudyController.class);

    private final StudyService studyService;
    private final KeycloakService keycloakService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService; // <-- NEW

    @Autowired
    public StudyController(StudyService studyService,
                           KeycloakService keycloakService,
                           RoleCheckerService roleCheckerService,
                           AuditLogBookService auditLogBookService) {
        this.studyService = studyService;
        this.keycloakService = keycloakService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Retrieves all studies or a single study if studyId param is provided.
     * @param studyId   Optional ID of the study
     * @param principal Jwt principal containing user info
     * @return List of studies or single
     */
    @GetMapping
    public ResponseEntity<List<Study>> getStudies(@RequestParam(required = false) Long studyId,
                                                  @AuthenticationPrincipal Jwt principal) {

        List<Study> studies;
        if (studyId != null) {
            Optional<Study> studyOpt = studyService.findStudyByStudyId(studyId);
            studies = studyOpt.map(List::of).orElse(List.of());
        } else {
            studies = studyService.getAllStudies();
        }

        long totalCount = studies.size();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(studies);
    }

    /**
     * Retrieves a single study by its ID.
     * Requires STUDY_OWNER role.
     *
     * @param studyId   ID of the study
     * @param principal Jwt principal containing user info
     * @return Study or 403 if unauthorized
     */
    @GetMapping("/{studyId}")
    public ResponseEntity<?> getStudy(@PathVariable Long studyId,
                                      @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.hasAnyRole(principal, List.of(Role.STUDY_OWNER))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String userId = principal.getSubject();

        if (!keycloakService.isUserInStudyGroupWithRoles(studyId, userId, List.of("STUDY_OWNER"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Study> study = studyService.findStudyByStudyId(studyId);
        return study.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new Study and assigns the creator as STUDY_OWNER.
     *
     * @param study     Study object to create
     * @param principal Jwt principal containing user info
     * @return Created Study or BAD_REQUEST on error
     */
    @PostMapping
    public ResponseEntity<?> createStudy(@RequestBody Study study,
                                         @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.hasAnyRole(principal, List.of(Role.STUDY_OWNER))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            String ownerId = principal.getSubject();
            study.setOwner(ownerId);

            Study savedStudy = studyService.saveStudy(study);

            keycloakService.createStudyGroups(savedStudy.getId(), ownerId);
            keycloakService.assignPersonnelToStudyGroups(savedStudy.getId(), ownerId, List.of("STUDY_OWNER"));

            // Audit log
            if (savedStudy.getId() != null) {
                String recordId = savedStudy.getId().toString();
                String description = "Creation of Study " + recordId;
                auditLogBookService.createAuditLog(
                        ownerId,
                        principal.getClaim("preferred_username"),
                        savedStudy.getId(),
                        "CREATE",
                        "Study",
                        recordId,
                        savedStudy,
                        description
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(savedStudy);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Updates an existing Study if the caller has STUDY_OWNER role.
     *
     * @param studyId       ID of the study to update
     * @param updatedStudy  Updated details
     * @param principal     Jwt principal containing user info
     * @return Updated Study or NOT_FOUND
     */
    @PutMapping("/{studyId}")
    public ResponseEntity<?> updateStudy(@PathVariable Long studyId,
                                         @RequestBody Study updatedStudy,
                                         @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.hasAnyRole(principal, List.of(Role.STUDY_OWNER))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String userId = principal.getSubject();

        if (!keycloakService.isUserInStudyGroupWithRoles(studyId, userId, List.of("STUDY_OWNER"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Study> savedStudyOpt = studyService.updateStudy(studyId, updatedStudy);
        if (savedStudyOpt.isPresent()) {
            Study savedStudy = savedStudyOpt.get();
            if (savedStudy.getId() != null) {
                String recordId = savedStudy.getId().toString();
                String description = "Update of Study " + recordId;
                auditLogBookService.createAuditLog(
                        userId,
                        principal.getClaim("preferred_username"),
                        studyId,
                        "UPDATE",
                        "Study",
                        recordId,
                        savedStudy,
                        description
                );
            }
            return ResponseEntity.ok(savedStudy);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes an existing Study if caller has STUDY_OWNER role.
     *
     * @param studyId   ID of the study to delete
     * @param principal Jwt principal containing user info
     * @return No content or NOT_FOUND
     */
    @DeleteMapping("/{studyId}")
    public ResponseEntity<?> deleteStudy(@PathVariable Long studyId,
                                         @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.hasAnyRole(principal, List.of(Role.STUDY_OWNER))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String userId = principal.getSubject();
        String username = principal.getClaim("preferred_username");
        if (!keycloakService.isUserInStudyGroupWithRoles(studyId, userId, List.of("STUDY_OWNER"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean isDeleted = studyService.deleteStudy(studyId);
        if (isDeleted) {
            String description = "Deletion of Study " + studyId;
            auditLogBookService.createAuditLog(
                    userId,
                    username,
                    studyId,
                    "DELETE",
                    "Study",
                    studyId.toString(),
                    null,
                    description
            );
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
