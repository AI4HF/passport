package io.passport.server.controller;

import io.passport.server.model.*;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.LearningProcessDatasetService;
import io.passport.server.service.RoleCheckerService;
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
import java.util.stream.Collectors;

/**
 * Class which stores the generated HTTP requests related to LearningProcessDataset operations.
 */
@RestController
@RequestMapping("/learning-process-dataset")
public class LearningProcessDatasetController {

    private static final Logger log = LoggerFactory.getLogger(LearningProcessDatasetController.class);

    private final String relationName = "Learning Process Dataset";
    private final LearningProcessDatasetService learningProcessDatasetService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    private final List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);

    @Autowired
    public LearningProcessDatasetController(LearningProcessDatasetService learningProcessDatasetService,
                                            RoleCheckerService roleCheckerService,
                                            AuditLogBookService auditLogBookService) {
        this.learningProcessDatasetService = learningProcessDatasetService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Read all LearningProcessDatasets or filtered by learningProcessId and/or learningDatasetId.
     *
     * @param studyId           ID of the study for authorization
     * @param learningProcessId ID of the LearningProcess (optional)
     * @param learningDatasetId ID of the LearningDataset (optional)
     * @param principal         Jwt principal containing user info
     * @return List of LearningProcessDatasetDTOs
     */
    @GetMapping
    public ResponseEntity<List<LearningProcessDatasetDTO>> getLearningProcessDatasets(
            @RequestParam String studyId,
            @RequestParam(required = false) String learningProcessId,
            @RequestParam(required = false) String learningDatasetId,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<LearningProcessDataset> datasets;

        if (learningProcessId != null && learningDatasetId != null) {
            LearningProcessDatasetId id = new LearningProcessDatasetId();
            id.setLearningDatasetId(learningDatasetId);
            id.setLearningProcessId(learningProcessId);
            Optional<LearningProcessDataset> one = this.learningProcessDatasetService.findLearningProcessDatasetById(id);
            datasets = one.map(List::of).orElseGet(List::of);
        } else if (learningProcessId != null) {
            datasets = this.learningProcessDatasetService.findByLearningProcessId(learningProcessId);
        } else if (learningDatasetId != null) {
            datasets = this.learningProcessDatasetService.findByLearningDatasetId(learningDatasetId);
        } else {
            datasets = this.learningProcessDatasetService.getAllLearningProcessDatasets();
        }

        List<LearningProcessDatasetDTO> dtos = datasets.stream()
                .map(LearningProcessDatasetDTO::new)
                .collect(Collectors.toList());

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(dtos.size()));
        return ResponseEntity.ok().headers(headers).body(dtos);
    }

    /**
     * Create a new LearningProcessDataset entity.
     *
     * @param studyId                   ID of the study for authorization
     * @param learningProcessDatasetDTO DTO with the new entity data
     * @param principal                 Jwt principal containing user info
     * @return Created LearningProcessDataset
     */
    @PostMapping
    public ResponseEntity<?> createLearningProcessDataset(@RequestParam String studyId,
                                                          @RequestBody LearningProcessDatasetDTO learningProcessDatasetDTO,
                                                          @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            LearningProcessDataset entity = new LearningProcessDataset(learningProcessDatasetDTO);
            LearningProcessDataset saved = this.learningProcessDatasetService.saveLearningProcessDataset(entity);

            if (saved.getId() != null) {
                String lpId = saved.getId().getLearningProcessId();
                String ldId = saved.getId().getLearningDatasetId();
                String compositeId = "(" + lpId + ", " + ldId + ")";
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
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {
            log.error("Error creating LearningProcessDataset: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update LearningProcessDataset using query parameters.
     *
     * @param studyId                       ID of the study for authorization
     * @param learningProcessId             ID of the LearningProcess
     * @param learningDatasetId             ID of the LearningDataset
     * @param updatedLearningProcessDataset Updated entity data
     * @param principal                     Jwt principal containing user info
     * @return Updated LearningProcessDataset or NOT_FOUND
     */
    @PutMapping
    public ResponseEntity<?> updateLearningProcessDataset(
            @RequestParam String studyId,
            @RequestParam String learningProcessId,
            @RequestParam String learningDatasetId,
            @RequestBody LearningProcessDataset updatedLearningProcessDataset,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        LearningProcessDatasetId id = new LearningProcessDatasetId();

        id.setLearningDatasetId(learningDatasetId);
        id.setLearningProcessId(learningProcessId);

        try {
            Optional<LearningProcessDataset> savedOpt =
                    this.learningProcessDatasetService.updateLearningProcessDataset(id, updatedLearningProcessDataset);

            if (savedOpt.isPresent()) {
                LearningProcessDataset saved = savedOpt.get();
                String compositeId = "(" + learningProcessId + ", " + learningDatasetId + ")";
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.UPDATE,
                        relationName,
                        compositeId,
                        saved
                );
                return ResponseEntity.ok(saved);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error updating LearningProcessDataset: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by LearningProcessDataset composite ID using query parameters.
     *
     * @param studyId           ID of the study for authorization
     * @param learningProcessId ID of the LearningProcess
     * @param learningDatasetId ID of the LearningDataset
     * @param principal         Jwt principal containing user info
     * @return No content or NOT_FOUND
     */
    @DeleteMapping
    public ResponseEntity<?> deleteLearningProcessDataset(
            @RequestParam String studyId,
            @RequestParam String learningProcessId,
            @RequestParam String learningDatasetId,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        LearningProcessDatasetId id = new LearningProcessDatasetId();
        id.setLearningDatasetId(learningDatasetId);
        id.setLearningProcessId(learningProcessId);

        try {
            Optional<LearningProcessDataset> deletedLearningProcessDataset = this.learningProcessDatasetService.deleteLearningProcessDataset(id);
            if (deletedLearningProcessDataset.isPresent()) {
                String compositeId = "(" + learningProcessId + ", " + learningDatasetId + ")";
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.DELETE,
                        relationName,
                        compositeId,
                        deletedLearningProcessDataset.get()
                );
                return ResponseEntity.status(HttpStatus.OK).body(deletedLearningProcessDataset.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting LearningProcessDataset: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
