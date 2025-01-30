package io.passport.server.controller;

import io.passport.server.model.Role;
import io.passport.server.model.Study;
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

import static java.lang.Thread.sleep;

/**
 * Controller class for managing HTTP requests related to study operations.
 */
@RestController
@RequestMapping("/study")
public class StudyController {

    private static final Logger log = LoggerFactory.getLogger(StudyController.class);

    private final StudyService studyService;
    private final KeycloakService keycloakService;

    /**
     * Role checker service for authorization
     */
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public StudyController(StudyService studyService, KeycloakService keycloakService, RoleCheckerService roleCheckerService) {
        this.studyService = studyService;
        this.keycloakService = keycloakService;
        this.roleCheckerService = roleCheckerService;
    }

    @GetMapping()
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

    @GetMapping("/{studyId}")
    public ResponseEntity<?> getStudy(@PathVariable Long studyId, @AuthenticationPrincipal Jwt principal) {

        if(!this.roleCheckerService.hasAnyRole(principal, List.of(Role.STUDY_OWNER))){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String userId = principal.getSubject();

        if (!keycloakService.isUserInStudyGroupWithRoles(studyId, userId, List.of("STUDY_OWNER"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Study> study = studyService.findStudyByStudyId(studyId);
        return study.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping()
    public ResponseEntity<?> createStudy(@RequestBody Study study, @AuthenticationPrincipal Jwt principal) {

        if(!this.roleCheckerService.hasAnyRole(principal, List.of(Role.STUDY_OWNER))){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            String ownerId = principal.getSubject();
            study.setOwner(ownerId);

            Study savedStudy = studyService.saveStudy(study);

            keycloakService.createStudyGroups(study.getId(), ownerId);
            keycloakService.assignPersonnelToStudyGroups(savedStudy.getId(), ownerId, List.of("STUDY_OWNER"));

            return ResponseEntity.status(HttpStatus.CREATED).body(savedStudy);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{studyId}")
    public ResponseEntity<?> updateStudy(@PathVariable Long studyId,
                                         @RequestBody Study updatedStudy,
                                         @AuthenticationPrincipal Jwt principal) {

        if(!this.roleCheckerService.hasAnyRole(principal, List.of(Role.STUDY_OWNER))){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String userId = principal.getSubject();

        if (!keycloakService.isUserInStudyGroupWithRoles(studyId, userId, List.of("STUDY_OWNER"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Study> savedStudy = studyService.updateStudy(studyId, updatedStudy);
        return savedStudy.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{studyId}")
    public ResponseEntity<?> deleteStudy(@PathVariable Long studyId,
                                         @AuthenticationPrincipal Jwt principal) {
        if(!this.roleCheckerService.hasAnyRole(principal, List.of(Role.STUDY_OWNER))){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String userId = principal.getSubject();

        if (!keycloakService.isUserInStudyGroupWithRoles(studyId, userId, List.of("STUDY_OWNER"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean isDeleted = studyService.deleteStudy(studyId);
        return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

