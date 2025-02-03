package io.passport.server.controller;

import io.passport.server.model.LearningProcessDataset;
import io.passport.server.model.LearningProcessDatasetDTO;
import io.passport.server.model.LearningProcessDatasetId;
import io.passport.server.model.Role;
import io.passport.server.service.AuditLogBookService; // <-- NEW
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

    private final LearningProcessDatasetService learningProcessDatasetService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService; // <-- NEW

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
     * @param studyId ID of the study for authorization
     * @param learningProcessId ID of the LearningProcess (optional)
     * @param learningDatasetId ID of the LearningDataset (optional)
     * @param principal Jwt principal containing user info
     * @return List of LearningProcessDatasetDTOs
     */
    @GetMapping
    public ResponseEntity<List<LearningProcessDatasetDTO>> getLearningProcessDatasets(
            @RequestParam Long studyId,
            @RequestParam(required = false) Long learningProcessId,
            @RequestParam(required = false) Long learningDatasetId,
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
     * @param studyId ID of the study for authorization
     * @param learningProcessDatasetDTO DTO with the new entity data
     * @param principal Jwt principal containing user info
     * @return Created LearningProcessDataset
     */
    @PostMapping
    public ResponseEntity<?> createLearningProcessDataset(@RequestParam Long studyId,
                                                          @RequestBody LearningProcessDatasetDTO learningProcessDatasetDTO,
                                                          @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            LearningProcessDataset entity = new LearningProcessDataset(learningProcessDatasetDTO);
            LearningProcessDataset saved = this.learningProcessDatasetService.saveLearningProcessDataset(entity);

            if (saved.getId() != null) {
                Long lpId = saved.getId().getLearningProcessId();
                Long ldId = saved.getId().getLearningDatasetId();
                String compositeId = "(" + lpId + ", " + ldId + ")";
                String description = "Creation of LearningProcessDataset with learningProcessId="
                        + lpId + " and learningDatasetId=" + ldId;
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        studyId,
                        "CREATE",
                        "LearningProcessDataset",
                        compositeId,
                        saved,
                        description
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
     * @param studyId ID of the study for authorization
     * @param learningProcessId ID of the LearningProcess
     * @param learningDatasetId ID of the LearningDataset
     * @param updatedLearningProcessDataset Updated entity data
     * @param principal Jwt principal containing user info
     * @return Updated LearningProcessDataset or NOT_FOUND
     */
    @PutMapping
    public ResponseEntity<?> updateLearningProcessDataset(
            @RequestParam Long studyId,
            @RequestParam Long learningProcessId,
            @RequestParam Long learningDatasetId,
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
                if (saved.getId() != null) {
                    String compositeId = "(" + learningProcessId + ", " + learningDatasetId + ")";
                    String description = "Update of LearningProcessDataset with learningProcessId="
                            + learningProcessId + " and learningDatasetId=" + learningDatasetId;
                    auditLogBookService.createAuditLog(
                            principal.getSubject(),
                            studyId,
                            "UPDATE",
                            "LearningProcessDataset",
                            compositeId,
                            saved,
                            description
                    );
                }
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
     * @param studyId ID of the study for authorization
     * @param learningProcessId ID of the LearningProcess
     * @param learningDatasetId ID of the LearningDataset
     * @param principal Jwt principal containing user info
     * @return No content or NOT_FOUND
     */
    @DeleteMapping
    public ResponseEntity<?> deleteLearningProcessDataset(
            @RequestParam Long studyId,
            @RequestParam Long learningProcessId,
            @RequestParam Long learningDatasetId,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        LearningProcessDatasetId id = new LearningProcessDatasetId();
        id.setLearningDatasetId(learningDatasetId);
        id.setLearningProcessId(learningProcessId);

        try {
            boolean isDeleted = this.learningProcessDatasetService.deleteLearningProcessDataset(id);
            if (isDeleted) {
                String compositeId = "(" + learningProcessId + ", " + learningDatasetId + ")";
                String description = "Deletion of LearningProcessDataset with learningProcessId="
                        + learningProcessId + " and learningDatasetId=" + learningDatasetId;
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        studyId,
                        "DELETE",
                        "LearningProcessDataset",
                        compositeId,
                        null,
                        description
                );
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting LearningProcessDataset: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
