package io.passport.server.controller;

import io.passport.server.model.*;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.LearningProcessParameterService;
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
 * Class which stores the generated HTTP requests related to LearningProcessParameter operations.
 */
@RestController
@RequestMapping("/learning-process-parameter")
public class LearningProcessParameterController {

    private static final Logger log = LoggerFactory.getLogger(LearningProcessParameterController.class);

    private final String relationName = "Learning Process Parameter";
    private final LearningProcessParameterService learningProcessParameterService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    private final List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);

    @Autowired
    public LearningProcessParameterController(LearningProcessParameterService learningProcessParameterService,
                                              RoleCheckerService roleCheckerService,
                                              AuditLogBookService auditLogBookService) {
        this.learningProcessParameterService = learningProcessParameterService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Read all LearningProcessParameters or filtered by learningProcessId and/or parameterId
     *
     * @param studyId           ID of the study for authorization
     * @param learningProcessId ID of the LearningProcess (optional)
     * @param parameterId       ID of the Parameter (optional)
     * @param principal         Jwt principal containing user info
     * @return List of LearningProcessParameterDTO
     */
    @GetMapping
    public ResponseEntity<List<LearningProcessParameterDTO>> getLearningProcessParameters(
            @RequestParam String studyId,
            @RequestParam(required = false) String learningProcessId,
            @RequestParam(required = false) String parameterId,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<LearningProcessParameter> parameters;
        if (learningProcessId != null && parameterId != null) {
            LearningProcessParameterId id = new LearningProcessParameterId();
            id.setLearningProcessId(learningProcessId);
            id.setParameterId(parameterId);
            Optional<LearningProcessParameter> one = this.learningProcessParameterService.findLearningProcessParameterById(id);
            parameters = one.map(List::of).orElseGet(List::of);
        } else if (learningProcessId != null) {
            parameters = this.learningProcessParameterService.findByLearningProcessId(learningProcessId);
        } else if (parameterId != null) {
            parameters = this.learningProcessParameterService.findByParameterId(parameterId);
        } else {
            parameters = this.learningProcessParameterService.getAllLearningProcessParameters();
        }

        List<LearningProcessParameterDTO> dtos = parameters.stream()
                .map(LearningProcessParameterDTO::new)
                .collect(Collectors.toList());

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(dtos.size()));
        return ResponseEntity.ok().headers(headers).body(dtos);
    }

    /**
     * Create a new LearningProcessParameter entity.
     *
     * @param studyId                     ID of the study for authorization
     * @param learningProcessParameterDTO DTO for the new entity
     * @param principal                   Jwt principal containing user info
     * @return Created LearningProcessParameter
     */
    @PostMapping
    public ResponseEntity<?> createLearningProcessParameter(@RequestParam String studyId,
                                                            @RequestBody LearningProcessParameterDTO learningProcessParameterDTO,
                                                            @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            LearningProcessParameter entity = new LearningProcessParameter(learningProcessParameterDTO);
            LearningProcessParameter saved = this.learningProcessParameterService.saveLearningProcessParameter(entity);

            if (saved.getId() != null) {
                String lpId = saved.getId().getLearningProcessId();
                String pId = saved.getId().getParameterId();
                String compositeId = "(" + lpId + ", " + pId + ")";
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
            log.error("Error creating LearningProcessParameter: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update LearningProcessParameter using query parameters.
     *
     * @param studyId                         ID of the study for authorization
     * @param learningProcessId               ID of the LearningProcess
     * @param parameterId                     ID of the Parameter
     * @param updatedLearningProcessParameter Updated data
     * @param principal                       Jwt principal containing user info
     * @return Updated LearningProcessParameter or NOT_FOUND
     */
    @PutMapping
    public ResponseEntity<?> updateLearningProcessParameter(
            @RequestParam String studyId,
            @RequestParam String learningProcessId,
            @RequestParam String parameterId,
            @RequestBody LearningProcessParameter updatedLearningProcessParameter,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        LearningProcessParameterId id = new LearningProcessParameterId();
        id.setLearningProcessId(learningProcessId);
        id.setParameterId(parameterId);

        try {
            Optional<LearningProcessParameter> savedOpt =
                    this.learningProcessParameterService.updateLearningProcessParameter(id, updatedLearningProcessParameter);

            if (savedOpt.isPresent()) {
                LearningProcessParameter saved = savedOpt.get();
                String compositeId = "(" + learningProcessId + ", " + parameterId + ")";
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
            log.error("Error updating LearningProcessParameter: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by LearningProcessParameter composite ID using query parameters.
     *
     * @param studyId           ID of the study for authorization
     * @param learningProcessId ID of the LearningProcess
     * @param parameterId       ID of the Parameter
     * @param principal         Jwt principal containing user info
     * @return No content or NOT_FOUND
     */
    @DeleteMapping
    public ResponseEntity<?> deleteLearningProcessParameter(
            @RequestParam String studyId,
            @RequestParam String learningProcessId,
            @RequestParam String parameterId,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        LearningProcessParameterId id = new LearningProcessParameterId();
        id.setLearningProcessId(learningProcessId);
        id.setParameterId(parameterId);

        try {
            Optional<LearningProcessParameter> deletedLearningProcessParameter = this.learningProcessParameterService.deleteLearningProcessParameter(id);
            if (deletedLearningProcessParameter.isPresent()) {
                String compositeId = "(" + learningProcessId + ", " + parameterId + ")";
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.DELETE,
                        relationName,
                        compositeId,
                        deletedLearningProcessParameter.get()
                );
                return ResponseEntity.status(HttpStatus.OK).body(deletedLearningProcessParameter.get());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error deleting LearningProcessParameter: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
