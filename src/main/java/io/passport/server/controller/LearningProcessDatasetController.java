package io.passport.server.controller;

import io.passport.server.model.LearningProcessDatasetDTO;
import io.passport.server.model.LearningProcessDataset;
import io.passport.server.model.LearningProcessDatasetId;
import io.passport.server.model.Role;
import io.passport.server.service.LearningProcessDatasetService;
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
 * Class which stores the generated HTTP requests related to LearningProcessDataset operations.
 */
@RestController
@RequestMapping("/learning-process-dataset")
public class LearningProcessDatasetController {
    private static final Logger log = LoggerFactory.getLogger(LearningProcessDatasetController.class);

    /**
     * LearningProcessDataset service for LearningProcessDataset management
     */
    private final LearningProcessDatasetService learningProcessDatasetService;

    /**
     * Role checker service for authorization
     */
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public LearningProcessDatasetController(LearningProcessDatasetService learningProcessDatasetService, RoleCheckerService roleCheckerService) {
        this.learningProcessDatasetService = learningProcessDatasetService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read all LearningProcessDatasets or filtered by learningProcessId and/or learningDatasetId
     * @param learningProcessId ID of the LearningProcess (optional)
     * @param learningDatasetId ID of the LearningDataset (optional)
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<LearningProcessDatasetDTO>> getLearningProcessDatasets(
            @RequestParam(required = false) Long learningProcessId,
            @RequestParam(required = false) Long learningDatasetId,
            @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<LearningProcessDataset> datasets;

        if (learningProcessId != null && learningDatasetId != null) {
            LearningProcessDatasetId id = new LearningProcessDatasetId();
            id.setLearningProcessId(learningProcessId);
            id.setLearningDatasetId(learningDatasetId);
            Optional<LearningProcessDataset> dataset = this.learningProcessDatasetService.findLearningProcessDatasetById(id);
            datasets = dataset.map(List::of).orElseGet(List::of);
        } else if (learningProcessId != null) {
            datasets = this.learningProcessDatasetService.findByLearningProcessId(learningProcessId);
        } else if (learningDatasetId != null) {
            datasets = this.learningProcessDatasetService.findByLearningDatasetId(learningDatasetId);
        } else {
            datasets = this.learningProcessDatasetService.getAllLearningProcessDatasets();
        }

        List<LearningProcessDatasetDTO> dtos = datasets.stream()
                .map(entity -> new LearningProcessDatasetDTO(entity))
                .collect(Collectors.toList());

        long totalCount = dtos.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(dtos);
    }

    /**
     * Create a new LearningProcessDataset entity.
     * @param learningProcessDatasetDTO the DTO containing data for the new LearningProcessDataset with input structure
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createLearningProcessDataset(@RequestBody LearningProcessDatasetDTO learningProcessDatasetDTO,
                                                          @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try {

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            LearningProcessDataset learningProcessDataset = new LearningProcessDataset(learningProcessDatasetDTO);
            LearningProcessDataset savedLearningProcessDataset = this.learningProcessDatasetService.saveLearningProcessDataset(learningProcessDataset);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedLearningProcessDataset);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update LearningProcessDataset using query parameters.
     * @param learningProcessId ID of the LearningProcess
     * @param learningDatasetId ID of the LearningDataset
     * @param updatedLearningProcessDataset LearningProcessDataset model instance with updated details.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PutMapping()
    public ResponseEntity<?> updateLearningProcessDataset(
            @RequestParam Long learningProcessId,
            @RequestParam Long learningDatasetId,
            @RequestBody LearningProcessDataset updatedLearningProcessDataset,
            @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        LearningProcessDatasetId learningProcessDatasetId = new LearningProcessDatasetId();
        learningProcessDatasetId.setLearningProcessId(learningProcessId);
        learningProcessDatasetId.setLearningDatasetId(learningDatasetId);

        try {
            Optional<LearningProcessDataset> savedLearningProcessDataset = this.learningProcessDatasetService.updateLearningProcessDataset(learningProcessDatasetId, updatedLearningProcessDataset);
            if (savedLearningProcessDataset.isPresent()) {
                return ResponseEntity.ok().body(savedLearningProcessDataset);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by LearningProcessDataset composite ID using query parameters.
     * @param learningProcessId ID of the LearningProcess
     * @param learningDatasetId ID of the LearningDataset
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @DeleteMapping()
    public ResponseEntity<?> deleteLearningProcessDataset(
            @RequestParam Long learningProcessId,
            @RequestParam Long learningDatasetId,
            @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        LearningProcessDatasetId learningProcessDatasetId = new LearningProcessDatasetId();
        learningProcessDatasetId.setLearningProcessId(learningProcessId);
        learningProcessDatasetId.setLearningDatasetId(learningDatasetId);

        try {
            boolean isDeleted = this.learningProcessDatasetService.deleteLearningProcessDataset(learningProcessDatasetId);
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
