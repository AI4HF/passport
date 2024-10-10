package io.passport.server.controller;

import io.passport.server.model.LearningStageParameterDTO;
import io.passport.server.model.LearningStageParameter;
import io.passport.server.model.LearningStageParameterId;
import io.passport.server.model.Role;
import io.passport.server.service.LearningStageParameterService;
import io.passport.server.service.RoleCheckerService;
import org.keycloak.KeycloakPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);

    @Autowired
    public LearningStageParameterController(LearningStageParameterService learningStageParameterService, RoleCheckerService roleCheckerService) {
        this.learningStageParameterService = learningStageParameterService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Retrieve LearningStageParameters filtered by learningStageId and/or parameterId.
     * @param studyId ID of the study for authorization
     * @param learningStageId ID of the LearningStage (optional)
     * @param parameterId ID of the Parameter (optional)
     * @param principal KeycloakPrincipal object that holds access token
     * @return List of LearningStageParameters
     */
    @GetMapping()
    public ResponseEntity<List<LearningStageParameterDTO>> getLearningStageParameters(
            @RequestParam Long studyId,
            @RequestParam(required = false) Long learningStageId,
            @RequestParam(required = false) Long parameterId,
            @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<LearningStageParameter> parameters;

        if (learningStageId != null && parameterId != null) {
            LearningStageParameterId id = new LearningStageParameterId();
            id.setLearningStageId(learningStageId);
            id.setParameterId(parameterId);
            Optional<LearningStageParameter> parameter = this.learningStageParameterService.findLearningStageParameterById(id);
            parameters = parameter.map(List::of).orElseGet(List::of);
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
     * @param learningStageParameterDTO DTO containing LearningStageParameter data
     * @param principal KeycloakPrincipal object that holds access token
     * @return ResponseEntity with created LearningStageParameter
     */
    @PostMapping()
    public ResponseEntity<?> createLearningStageParameter(@RequestParam Long studyId,
                                                          @RequestBody LearningStageParameterDTO learningStageParameterDTO,
                                                          @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            LearningStageParameter learningStageParameter = new LearningStageParameter(learningStageParameterDTO);
            LearningStageParameter savedLearningStageParameter = this.learningStageParameterService.saveLearningStageParameter(learningStageParameter);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedLearningStageParameter);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update LearningStageParameter using composite ID.
     * @param studyId ID of the study for authorization
     * @param learningStageId ID of the LearningStage
     * @param parameterId ID of the Parameter
     * @param updatedLearningStageParameter LearningStageParameter model instance with updated details
     * @param principal KeycloakPrincipal object that holds access token
     * @return ResponseEntity with updated LearningStageParameter or not found status
     */
    @PutMapping()
    public ResponseEntity<?> updateLearningStageParameter(
            @RequestParam Long studyId,
            @RequestParam Long learningStageId,
            @RequestParam Long parameterId,
            @RequestBody LearningStageParameter updatedLearningStageParameter,
            @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        LearningStageParameterId id = new LearningStageParameterId();
        id.setParameterId(parameterId);
        id.setLearningStageId(learningStageId);

        try {
            Optional<LearningStageParameter> savedLearningStageParameter = this.learningStageParameterService.updateLearningStageParameter(id, updatedLearningStageParameter);
            return savedLearningStageParameter.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete LearningStageParameter by composite ID.
     * @param studyId ID of the study for authorization
     * @param learningStageId ID of the LearningStage
     * @param parameterId ID of the Parameter
     * @param principal KeycloakPrincipal object that holds access token
     * @return ResponseEntity with no content status or not found status
     */
    @DeleteMapping()
    public ResponseEntity<?> deleteLearningStageParameter(@RequestParam Long studyId,
                                                          @RequestParam Long learningStageId,
                                                          @RequestParam Long parameterId,
                                                          @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        LearningStageParameterId id = new LearningStageParameterId();
        id.setLearningStageId(learningStageId);
        id.setParameterId(parameterId);

        try {
            boolean isDeleted = this.learningStageParameterService.deleteLearningStageParameter(id);
            return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
