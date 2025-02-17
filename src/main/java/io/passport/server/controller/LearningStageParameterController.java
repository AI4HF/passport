package io.passport.server.controller;

import io.passport.server.model.LearningStageParameter;
import io.passport.server.model.LearningStageParameterDTO;
import io.passport.server.model.LearningStageParameterId;
import io.passport.server.model.Role;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.LearningStageParameterService;
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
 * Class which stores the generated HTTP requests related to LearningStageParameter operations.
 */
@RestController
@RequestMapping("/learning-stage-parameter")
public class LearningStageParameterController {

    private static final Logger log = LoggerFactory.getLogger(LearningStageParameterController.class);

    private final LearningStageParameterService learningStageParameterService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService; // <-- NEW

    private final List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);

    @Autowired
    public LearningStageParameterController(LearningStageParameterService learningStageParameterService,
                                            RoleCheckerService roleCheckerService,
                                            AuditLogBookService auditLogBookService) {
        this.learningStageParameterService = learningStageParameterService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Retrieve LearningStageParameters filtered by learningStageId and/or parameterId.
     * @param studyId ID of the study for authorization
     * @param learningStageId ID of the LearningStage (optional)
     * @param parameterId ID of the Parameter (optional)
     * @param principal Jwt principal containing user info
     * @return List of LearningStageParameterDTO
     */
    @GetMapping
    public ResponseEntity<List<LearningStageParameterDTO>> getLearningStageParameters(
            @RequestParam Long studyId,
            @RequestParam(required = false) Long learningStageId,
            @RequestParam(required = false) Long parameterId,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<LearningStageParameter> parameters;
        if (learningStageId != null && parameterId != null) {
            LearningStageParameterId id = new LearningStageParameterId();
            id.setParameterId(parameterId);
            id.setLearningStageId(learningStageId);
            Optional<LearningStageParameter> one = this.learningStageParameterService.findLearningStageParameterById(id);
            parameters = one.map(List::of).orElseGet(List::of);
        } else if (learningStageId != null) {
            parameters = this.learningStageParameterService.findByLearningStageId(learningStageId);
        } else if (parameterId != null) {
            parameters = this.learningStageParameterService.findByParameterId(parameterId);
        } else {
            parameters = this.learningStageParameterService.getAllLearningStageParameters();
        }

        List<LearningStageParameterDTO> dtos = parameters.stream()
                .map(LearningStageParameterDTO::new)
                .collect(Collectors.toList());

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(dtos.size()));
        return ResponseEntity.ok().headers(headers).body(dtos);
    }

    /**
     * Create a new LearningStageParameter.
     * @param studyId ID of the study for authorization
     * @param learningStageParameterDTO DTO containing data
     * @param principal Jwt principal containing user info
     * @return Created LearningStageParameter
     */
    @PostMapping
    public ResponseEntity<?> createLearningStageParameter(@RequestParam Long studyId,
                                                          @RequestBody LearningStageParameterDTO learningStageParameterDTO,
                                                          @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            LearningStageParameter entity = new LearningStageParameter(learningStageParameterDTO);
            LearningStageParameter saved = this.learningStageParameterService.saveLearningStageParameter(entity);

            if (saved.getId() != null) {
                Long lsId = saved.getId().getLearningStageId();
                Long pId = saved.getId().getParameterId();
                String compositeId = "(" + lsId + ", " + pId + ")";
                String description = "Creation of LearningStageParameter with learningStageId="
                        + lsId + " and parameterId=" + pId;
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim("preferred_username"),
                        studyId,
                        "CREATE",
                        "LearningStageParameter",
                        compositeId,
                        saved,
                        description
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {
            log.error("Error creating LearningStageParameter: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update LearningStageParameter using composite ID.
     * @param studyId ID of the study for authorization
     * @param learningStageId ID of the LearningStage
     * @param parameterId ID of the Parameter
     * @param updatedLearningStageParameter Updated data
     * @param principal Jwt principal containing user info
     * @return Updated LearningStageParameter or NOT_FOUND
     */
    @PutMapping
    public ResponseEntity<?> updateLearningStageParameter(
            @RequestParam Long studyId,
            @RequestParam Long learningStageId,
            @RequestParam Long parameterId,
            @RequestBody LearningStageParameter updatedLearningStageParameter,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        LearningStageParameterId id = new LearningStageParameterId();
        id.setParameterId(parameterId);
        id.setLearningStageId(learningStageId);

        try {
            Optional<LearningStageParameter> savedOpt =
                    this.learningStageParameterService.updateLearningStageParameter(id, updatedLearningStageParameter);

            if (savedOpt.isPresent()) {
                LearningStageParameter saved = savedOpt.get();
                if (saved.getId() != null) {
                    String compositeId = "(" + learningStageId + ", " + parameterId + ")";
                    String description = "Update of LearningStageParameter with learningStageId="
                            + learningStageId + " and parameterId=" + parameterId;
                    auditLogBookService.createAuditLog(
                            principal.getSubject(),
                            principal.getClaim("preferred_username"),
                            studyId,
                            "UPDATE",
                            "LearningStageParameter",
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
            log.error("Error updating LearningStageParameter: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete LearningStageParameter by composite ID.
     * @param studyId ID of the study for authorization
     * @param learningStageId ID of the LearningStage
     * @param parameterId ID of the Parameter
     * @param principal Jwt principal containing user info
     * @return No content or NOT_FOUND
     */
    @DeleteMapping
    public ResponseEntity<?> deleteLearningStageParameter(@RequestParam Long studyId,
                                                          @RequestParam Long learningStageId,
                                                          @RequestParam Long parameterId,
                                                          @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        LearningStageParameterId id = new LearningStageParameterId();
        id.setParameterId(parameterId);
        id.setLearningStageId(learningStageId);

        try {
            boolean isDeleted = this.learningStageParameterService.deleteLearningStageParameter(id);
            if (isDeleted) {
                String compositeId = "(" + learningStageId + ", " + parameterId + ")";
                String description = "Deletion of LearningStageParameter with learningStageId="
                        + learningStageId + " and parameterId=" + parameterId;
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim("preferred_username"),
                        studyId,
                        "DELETE",
                        "LearningStageParameter",
                        compositeId,
                        null,
                        description
                );
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error deleting LearningStageParameter: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
