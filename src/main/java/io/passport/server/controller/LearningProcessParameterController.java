package io.passport.server.controller;

import io.passport.server.model.LearningProcessParameterDTO;
import io.passport.server.model.LearningProcessParameter;
import io.passport.server.model.LearningProcessParameterId;
import io.passport.server.model.Role;
import io.passport.server.service.LearningProcessParameterService;
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
 * Class which stores the generated HTTP requests related to LearningProcessParameter operations.
 */
@RestController
@RequestMapping("/learning-process-parameter")
public class LearningProcessParameterController {
    private static final Logger log = LoggerFactory.getLogger(LearningProcessParameterController.class);

    /**
     * LearningProcessParameter service for LearningProcessParameter management
     */
    private final LearningProcessParameterService learningProcessParameterService;

    /**
     * Role checker service for authorization
     */
    private final RoleCheckerService roleCheckerService;

    /**
     * List of authorized roles for this endpoint
     */
    private final List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);

    @Autowired
    public LearningProcessParameterController(LearningProcessParameterService learningProcessParameterService, RoleCheckerService roleCheckerService) {
        this.learningProcessParameterService = learningProcessParameterService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read all LearningProcessParameters or filtered by learningProcessId and/or parameterId
     * @param learningProcessId ID of the LearningProcess (optional)
     * @param parameterId ID of the Parameter (optional)
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<LearningProcessParameterDTO>> getLearningProcessParameters(
            @RequestParam(required = false) Long learningProcessId,
            @RequestParam(required = false) Long parameterId,
            @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<LearningProcessParameter> parameters;

        if (learningProcessId != null && parameterId != null) {
            LearningProcessParameterId id = new LearningProcessParameterId();
            id.setLearningProcessId(learningProcessId);
            id.setParameterId(parameterId);
            Optional<LearningProcessParameter> parameter = this.learningProcessParameterService.findLearningProcessParameterById(id);
            parameters = parameter.map(List::of).orElseGet(List::of);
        } else if (learningProcessId != null) {
            parameters = this.learningProcessParameterService.findByLearningProcessId(learningProcessId);
        } else if (parameterId != null) {
            parameters = this.learningProcessParameterService.findByParameterId(parameterId);
        } else {
            parameters = this.learningProcessParameterService.getAllLearningProcessParameters();
        }

        List<LearningProcessParameterDTO> dtos = parameters.stream()
                .map(entity -> new LearningProcessParameterDTO(entity))
                .collect(Collectors.toList());

        long totalCount = dtos.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(dtos);
    }

    /**
     * Create a new LearningProcessParameter entity.
     * @param learningProcessParameterDTO the DTO containing data for the new LearningProcessParameter
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createLearningProcessParameter(@RequestBody LearningProcessParameterDTO learningProcessParameterDTO,
                                                            @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try {

            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            LearningProcessParameter learningProcessParameter = new LearningProcessParameter(learningProcessParameterDTO);
            LearningProcessParameter savedLearningProcessParameter = this.learningProcessParameterService.saveLearningProcessParameter(learningProcessParameter);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedLearningProcessParameter);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update LearningProcessParameter using query parameters.
     * @param learningProcessId ID of the LearningProcess
     * @param parameterId ID of the Parameter
     * @param updatedLearningProcessParameter LearningProcessParameter model instance with updated details.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PutMapping()
    public ResponseEntity<?> updateLearningProcessParameter(
            @RequestParam Long learningProcessId,
            @RequestParam Long parameterId,
            @RequestBody LearningProcessParameter updatedLearningProcessParameter,
            @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        LearningProcessParameterId learningProcessParameterId = new LearningProcessParameterId();
        learningProcessParameterId.setLearningProcessId(learningProcessId);
        learningProcessParameterId.setParameterId(parameterId);

        try {
            Optional<LearningProcessParameter> savedLearningProcessParameter = this.learningProcessParameterService.updateLearningProcessParameter(learningProcessParameterId, updatedLearningProcessParameter);
            if (savedLearningProcessParameter.isPresent()) {
                return ResponseEntity.ok().body(savedLearningProcessParameter);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by LearningProcessParameter composite ID using query parameters.
     * @param learningProcessId ID of the LearningProcess
     * @param parameterId ID of the Parameter
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @DeleteMapping()
    public ResponseEntity<?> deleteLearningProcessParameter(
            @RequestParam Long learningProcessId,
            @RequestParam Long parameterId,
            @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        LearningProcessParameterId learningProcessParameterId = new LearningProcessParameterId();
        learningProcessParameterId.setLearningProcessId(learningProcessId);
        learningProcessParameterId.setParameterId(parameterId);

        try {
            boolean isDeleted = this.learningProcessParameterService.deleteLearningProcessParameter(learningProcessParameterId);
            if (isDeleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

