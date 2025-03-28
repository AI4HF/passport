package io.passport.server.controller;

import io.passport.server.model.*;
import io.passport.server.service.AuditLogBookService; // <-- NEW
import io.passport.server.service.ModelParameterService;
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
 * Class which stores the generated HTTP requests related to ModelParameter operations.
 */
@RestController
@RequestMapping("/model-parameter")
public class ModelParameterController {

    private static final Logger log = LoggerFactory.getLogger(ModelParameterController.class);

    private final String relationName = "Model Parameter";
    private final ModelParameterService modelParameterService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    private final List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);

    @Autowired
    public ModelParameterController(ModelParameterService modelParameterService,
                                    RoleCheckerService roleCheckerService,
                                    AuditLogBookService auditLogBookService) {
        this.modelParameterService = modelParameterService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Read all ModelParameters or filtered by modelId and/or parameterId
     * @param studyId ID of the study for authorization
     * @param modelId ID of the Model (optional)
     * @param parameterId ID of the Parameter (optional)
     * @param principal Jwt principal containing user info
     * @return List of ModelParameterDTOs
     */
    @GetMapping
    public ResponseEntity<List<ModelParameterDTO>> getModelParameters(
            @RequestParam String studyId,
            @RequestParam(required = false) String modelId,
            @RequestParam(required = false) String parameterId,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ModelParameter> parameters;
        if (modelId != null && parameterId != null) {
            ModelParameterId id = new ModelParameterId(modelId, parameterId);
            Optional<ModelParameter> modelParameter = this.modelParameterService.findModelParameterById(id);
            parameters = modelParameter.map(List::of).orElseGet(List::of);
        } else if (modelId != null) {
            parameters = this.modelParameterService.findByModelId(modelId);
        } else if (parameterId != null) {
            parameters = this.modelParameterService.findByParameterId(parameterId);
        } else {
            parameters = this.modelParameterService.getAllModelParameters();
        }
        if (modelId != null && parameterId != null && parameters.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ModelParameterDTO> dtos = parameters.stream()
                .map(ModelParameterDTO::new)
                .collect(Collectors.toList());

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(dtos.size()));
        return ResponseEntity.ok().headers(headers).body(dtos);
    }

    /**
     * Create a new ModelParameter entity.
     * @param studyId ID of the study for authorization
     * @param modelParameterDTO DTO containing data for the new ModelParameter
     * @param principal Jwt principal containing user info
     * @return Created ModelParameter
     */
    @PostMapping
    public ResponseEntity<?> createModelParameter(@RequestParam String studyId,
                                                  @RequestBody ModelParameterDTO modelParameterDTO,
                                                  @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            ModelParameter entity = new ModelParameter(modelParameterDTO);
            ModelParameter saved = this.modelParameterService.saveModelParameter(entity);

            if (saved.getId() != null) {
                String mId = saved.getId().getModelId();
                String pId = saved.getId().getParameterId();
                String compositeId = "(" + mId + ", " + pId + ")";
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
            log.error("Error creating ModelParameter: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update ModelParameter using query parameters.
     * @param studyId ID of the study for authorization
     * @param modelId ID of the Model
     * @param parameterId ID of the Parameter
     * @param updatedModelParameter Updated details
     * @param principal Jwt principal containing user info
     * @return Updated ModelParameter or NOT_FOUND
     */
    @PutMapping
    public ResponseEntity<?> updateModelParameter(
            @RequestParam String studyId,
            @RequestParam String modelId,
            @RequestParam String parameterId,
            @RequestBody ModelParameter updatedModelParameter,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ModelParameterId id = new ModelParameterId(modelId, parameterId);

        try {
            Optional<ModelParameter> savedOpt = this.modelParameterService.updateModelParameter(id, updatedModelParameter);
            if (savedOpt.isPresent()) {
                ModelParameter saved = savedOpt.get();
                if (saved.getId() != null) {
                    String compositeId = "(" + modelId + ", " + parameterId + ")";
                    auditLogBookService.createAuditLog(
                            principal.getSubject(),
                            principal.getClaim(TokenClaim.USERNAME.getValue()),
                            studyId,
                            Operation.UPDATE,
                            relationName,
                            compositeId,
                            saved
                    );
                }
                return ResponseEntity.ok(saved);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error updating ModelParameter: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by ModelParameter composite ID using query parameters.
     * @param studyId ID of the study for authorization
     * @param modelId ID of the Model
     * @param parameterId ID of the Parameter
     * @param principal Jwt principal containing user info
     * @return No content or NOT_FOUND
     */
    @DeleteMapping
    public ResponseEntity<?> deleteModelParameter(
            @RequestParam String studyId,
            @RequestParam String modelId,
            @RequestParam String parameterId,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ModelParameterId id = new ModelParameterId(modelId, parameterId);

        try {
            Optional<ModelParameter> deletedModelParameter = this.modelParameterService.deleteModelParameter(id);
            if (deletedModelParameter.isPresent()) {
                String compositeId = "(" + modelId + ", " + parameterId + ")";
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.DELETE,
                        relationName,
                        compositeId,
                        deletedModelParameter.get()
                );
                return ResponseEntity.status(HttpStatus.OK).body(deletedModelParameter.get());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error deleting ModelParameter: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
