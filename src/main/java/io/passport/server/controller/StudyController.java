package io.passport.server.controller;

import io.passport.server.model.*;
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

    private final String relationName = "Study";
    private final StudyService studyService;
    private final KeycloakService keycloakService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

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
     *
     * @param studyId   Optional ID of the study
     * @param principal Jwt principal containing user info
     * @return List of studies or single
     */
    @GetMapping
    public ResponseEntity<List<Study>> getStudies(@RequestParam(required = false) String studyId,
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
    public ResponseEntity<?> getStudy(@PathVariable String studyId,
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

            if (savedStudy.getId() != null) {
                String recordId = savedStudy.getId();
                auditLogBookService.createAuditLog(
                        ownerId,
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        savedStudy.getId(),
                        Operation.CREATE,
                        relationName,
                        recordId,
                        savedStudy
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
     * @param studyId      ID of the study to update
     * @param updatedStudy Updated details
     * @param principal    Jwt principal containing user info
     * @return Updated Study or NOT_FOUND
     */
    @PutMapping("/{studyId}")
    public ResponseEntity<?> updateStudy(@PathVariable String studyId,
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
            String recordId = savedStudy.getId();
            auditLogBookService.createAuditLog(
                    userId,
                    principal.getClaim(TokenClaim.USERNAME.getValue()),
                    studyId,
                    Operation.UPDATE,
                    relationName,
                    recordId,
                    savedStudy
            );
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
    public ResponseEntity<?> deleteStudy(@PathVariable String studyId,
                                         @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.hasAnyRole(principal, List.of(Role.STUDY_OWNER))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String userId = principal.getSubject();
        String username = principal.getClaim("preferred_username");
        if (!keycloakService.isUserInStudyGroupWithRoles(studyId, userId, List.of("STUDY_OWNER"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Study> deletedStudy = studyService.deleteStudy(studyId);
        if (deletedStudy.isPresent()) {
            auditLogBookService.createAuditLog(
                    userId,
                    username,
                    studyId,
                    Operation.DELETE,
                    relationName,
                    studyId,
                    deletedStudy.get()
            );
            return ResponseEntity.status(HttpStatus.OK).body(deletedStudy.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
