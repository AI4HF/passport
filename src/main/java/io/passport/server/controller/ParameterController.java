package io.passport.server.controller;

import io.passport.server.model.Parameter;
import io.passport.server.model.Role;
import io.passport.server.service.AuditLogBookService; // <-- NEW
import io.passport.server.service.ParameterService;
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

/**
 * Class which stores the generated HTTP requests related to parameter operations.
 */
@RestController
@RequestMapping("/parameter")
public class ParameterController {

    private static final Logger log = LoggerFactory.getLogger(ParameterController.class);

    private final ParameterService parameterService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService; // <-- NEW

    private final List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);

    @Autowired
    public ParameterController(ParameterService parameterService,
                               RoleCheckerService roleCheckerService,
                               AuditLogBookService auditLogBookService) {
        this.parameterService = parameterService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Read all parameters by StudyId.
     * @param studyId ID of the study
     * @param principal Jwt principal containing user info
     * @return List of Parameters
     */
    @GetMapping
    public ResponseEntity<List<Parameter>> getAllParametersByStudyId(@RequestParam Long studyId,
                                                                     @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Parameter> parameters = this.parameterService.findParametersByStudyId(studyId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(parameters.size()));
        return ResponseEntity.ok().headers(headers).body(parameters);
    }

    /**
     * Read a parameter by id.
     * @param parameterId ID of the parameter
     * @param studyId ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Parameter entity or not found
     */
    @GetMapping("/{parameterId}")
    public ResponseEntity<?> getParameterById(@PathVariable Long parameterId,
                                              @RequestParam Long studyId,
                                              @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Parameter> parameter = this.parameterService.findParameterById(parameterId);
        return parameter.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create a Parameter.
     * @param parameter parameter model instance to be created
     * @param studyId ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Created parameter
     */
    @PostMapping
    public ResponseEntity<?> createParameter(@RequestBody Parameter parameter,
                                             @RequestParam Long studyId,
                                             @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Parameter savedParameter = this.parameterService.saveParameter(parameter);

            // Audit log
            if (savedParameter.getParameterId() != null) {
                String recordId = savedParameter.getParameterId().toString();
                String description = "Creation of Parameter " + recordId;
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        studyId,
                        "CREATE",
                        "Parameter",
                        recordId,
                        savedParameter,
                        description
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(savedParameter);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update a parameter.
     * @param parameterId ID of the parameter that is to be updated
     * @param updatedParameter model instance with updated details
     * @param studyId ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return Updated parameter
     */
    @PutMapping("/{parameterId}")
    public ResponseEntity<?> updateParameter(@PathVariable Long parameterId,
                                             @RequestBody Parameter updatedParameter,
                                             @RequestParam Long studyId,
                                             @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<Parameter> savedParameterOpt = this.parameterService.updateParameter(parameterId, updatedParameter);
            if (savedParameterOpt.isPresent()) {
                Parameter savedParameter = savedParameterOpt.get();
                if (savedParameter.getParameterId() != null) {
                    String recordId = savedParameter.getParameterId().toString();
                    String description = "Update of Parameter " + recordId;
                    auditLogBookService.createAuditLog(
                            principal.getSubject(),
                            studyId,
                            "UPDATE",
                            "Parameter",
                            recordId,
                            savedParameter,
                            description
                    );
                }
                return ResponseEntity.ok(savedParameter);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete a parameter by ID.
     * @param parameterId ID of the parameter that is to be deleted
     * @param studyId ID of the study for authorization
     * @param principal Jwt principal containing user info
     * @return No content or not found status
     */
    @DeleteMapping("/{parameterId}")
    public ResponseEntity<?> deleteParameter(@PathVariable Long parameterId,
                                             @RequestParam Long studyId,
                                             @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.parameterService.deleteParameter(parameterId);
            if (isDeleted) {
                String description = "Deletion of Parameter " + parameterId;
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        studyId,
                        "DELETE",
                        "Parameter",
                        parameterId.toString(),
                        null,
                        description
                );
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
