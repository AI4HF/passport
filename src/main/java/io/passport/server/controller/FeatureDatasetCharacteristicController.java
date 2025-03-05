package io.passport.server.controller;

import io.passport.server.model.*;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.FeatureDatasetCharacteristicService;
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
 * Class which stores the generated HTTP requests related to FeatureDatasetCharacteristic operations.
 */
@RestController
@RequestMapping("/feature-dataset-characteristic")
public class FeatureDatasetCharacteristicController {

    private static final Logger log = LoggerFactory.getLogger(FeatureDatasetCharacteristicController.class);

    private final String relationName = "Feature Dataset Characteristic";
    private final FeatureDatasetCharacteristicService featureDatasetCharacteristicService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);

    @Autowired
    public FeatureDatasetCharacteristicController(FeatureDatasetCharacteristicService featureDatasetCharacteristicService,
                                                  RoleCheckerService roleCheckerService,
                                                  AuditLogBookService auditLogBookService) {
        this.featureDatasetCharacteristicService = featureDatasetCharacteristicService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Read all FeatureDatasetCharacteristics or filtered by datasetId and/or featureId
     *
     * @param datasetId ID of the Dataset (optional)
     * @param featureId ID of the Feature (optional)
     * @param studyId   ID of the study
     * @param principal Jwt principal containing user info
     * @return List of FeatureDatasetCharacteristicDTO
     */
    @GetMapping
    public ResponseEntity<List<FeatureDatasetCharacteristicDTO>> getFeatureDatasetCharacteristics(
            @RequestParam(required = false) Long datasetId,
            @RequestParam(required = false) Long featureId,
            @RequestParam Long studyId,
            @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<FeatureDatasetCharacteristic> characteristics;

        if (datasetId != null && featureId != null) {
            FeatureDatasetCharacteristicId id = new FeatureDatasetCharacteristicId();
            id.setDatasetId(datasetId);
            id.setFeatureId(featureId);
            characteristics = this.featureDatasetCharacteristicService
                    .findFeatureDatasetCharacteristicById(id)
                    .map(List::of)
                    .orElseGet(List::of);
        } else if (datasetId != null) {
            characteristics = this.featureDatasetCharacteristicService.findByDatasetId(datasetId);
        } else if (featureId != null) {
            characteristics = this.featureDatasetCharacteristicService.findByFeatureId(featureId);
        } else {
            characteristics = this.featureDatasetCharacteristicService.getAllFeatureDatasetCharacteristics();
        }

        List<FeatureDatasetCharacteristicDTO> dtos = characteristics.stream()
                .map(FeatureDatasetCharacteristicDTO::new)
                .collect(Collectors.toList());

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(dtos.size()));
        return ResponseEntity.ok().headers(headers).body(dtos);
    }

    /**
     * Create a new FeatureDatasetCharacteristic entity.
     *
     * @param featureDatasetCharacteristicDTO DTO for the new FeatureDatasetCharacteristic
     * @param studyId                         ID of the study
     * @param principal                       Jwt principal containing user info
     * @return Created FeatureDatasetCharacteristic
     */
    @PostMapping
    public ResponseEntity<?> createFeatureDatasetCharacteristic(@RequestBody FeatureDatasetCharacteristicDTO featureDatasetCharacteristicDTO,
                                                                @RequestParam Long studyId,
                                                                @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            FeatureDatasetCharacteristic entity = new FeatureDatasetCharacteristic(featureDatasetCharacteristicDTO);
            FeatureDatasetCharacteristic saved = this.featureDatasetCharacteristicService.saveFeatureDatasetCharacteristic(entity);

            // Composite ID for logging
            if (saved.getId() != null) {
                Long dsId = saved.getId().getDatasetId();
                Long ftId = saved.getId().getFeatureId();
                String compositeId = "(" + dsId + ", " + ftId + ")";
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
            log.error("Error creating FeatureDatasetCharacteristic: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update FeatureDatasetCharacteristic using query parameters.
     *
     * @param datasetId                           ID of the Dataset
     * @param featureId                           ID of the Feature
     * @param updatedFeatureDatasetCharacteristic Updated details
     * @param studyId                             ID of the study
     * @param principal                           Jwt principal containing user info
     * @return Updated FeatureDatasetCharacteristic or NOT_FOUND
     */
    @PutMapping
    public ResponseEntity<?> updateFeatureDatasetCharacteristic(
            @RequestParam Long datasetId,
            @RequestParam Long featureId,
            @RequestBody FeatureDatasetCharacteristic updatedFeatureDatasetCharacteristic,
            @RequestParam Long studyId,
            @AuthenticationPrincipal Jwt principal) {

        FeatureDatasetCharacteristicId id = new FeatureDatasetCharacteristicId();
        id.setFeatureId(featureId);
        id.setDatasetId(datasetId);

        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<FeatureDatasetCharacteristic> savedOpt =
                    this.featureDatasetCharacteristicService.updateFeatureDatasetCharacteristic(id, updatedFeatureDatasetCharacteristic);

            if (savedOpt.isPresent()) {
                FeatureDatasetCharacteristic saved = savedOpt.get();
                String compositeId = "(" + id.getDatasetId() + ", " + id.getFeatureId() + ")";
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
            log.error("Error updating FeatureDatasetCharacteristic: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by FeatureDatasetCharacteristic composite ID using query parameters.
     *
     * @param datasetId ID of the Dataset
     * @param featureId ID of the Feature
     * @param studyId   ID of the study
     * @param principal Jwt principal containing user info
     * @return No content or NOT_FOUND
     */
    @DeleteMapping
    public ResponseEntity<?> deleteFeatureDatasetCharacteristic(
            @RequestParam Long datasetId,
            @RequestParam Long featureId,
            @RequestParam Long studyId,
            @AuthenticationPrincipal Jwt principal) {

        FeatureDatasetCharacteristicId id = new FeatureDatasetCharacteristicId();
        id.setFeatureId(featureId);
        id.setDatasetId(datasetId);

        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<FeatureDatasetCharacteristic> deletedFeatureDatasetCharacteristic = this.featureDatasetCharacteristicService.deleteFeatureDatasetCharacteristic(id);
            if (deletedFeatureDatasetCharacteristic.isPresent()) {
                String compositeId = "(" + datasetId + ", " + featureId + ")";
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.DELETE,
                        relationName,
                        compositeId,
                        deletedFeatureDatasetCharacteristic.get()
                );
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(deletedFeatureDatasetCharacteristic.get());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error deleting FeatureDatasetCharacteristic: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
