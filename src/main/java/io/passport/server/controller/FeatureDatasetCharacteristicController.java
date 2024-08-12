package io.passport.server.controller;

import io.passport.server.model.FeatureDatasetCharacteristicDTO;
import io.passport.server.model.FeatureDatasetCharacteristic;
import io.passport.server.model.FeatureDatasetCharacteristicId;
import io.passport.server.model.Role;
import io.passport.server.service.FeatureDatasetCharacteristicService;
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
 * Class which stores the generated HTTP requests related to FeatureDatasetCharacteristic operations.
 */
@RestController
@RequestMapping("/feature-dataset-characteristic")
public class FeatureDatasetCharacteristicController {
    private static final Logger log = LoggerFactory.getLogger(FeatureDatasetCharacteristicController.class);

    /**
     * FeatureDatasetCharacteristic service for FeatureDatasetCharacteristic management
     */
    private final FeatureDatasetCharacteristicService featureDatasetCharacteristicService;

    /**
     * Role checker service for authorization
     */
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public FeatureDatasetCharacteristicController(FeatureDatasetCharacteristicService featureDatasetCharacteristicService,
                                                  RoleCheckerService roleCheckerService) {
        this.featureDatasetCharacteristicService = featureDatasetCharacteristicService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read all FeatureDatasetCharacteristics or filtered by datasetId and/or featureId
     * @param datasetId ID of the Dataset (optional)
     * @param featureId ID of the Feature (optional)
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<FeatureDatasetCharacteristicDTO>> getFeatureDatasetCharacteristics(
            @RequestParam(required = false) Long datasetId,
            @RequestParam(required = false) Long featureId,
            @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);
        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<FeatureDatasetCharacteristic> characteristics;

        if (datasetId != null && featureId != null) {
            FeatureDatasetCharacteristicId id = new FeatureDatasetCharacteristicId();
            id.setDatasetId(datasetId);
            id.setFeatureId(featureId);
            Optional<FeatureDatasetCharacteristic> characteristic = this.featureDatasetCharacteristicService.findFeatureDatasetCharacteristicById(id);
            characteristics = characteristic.map(List::of).orElseGet(List::of);
        } else if (datasetId != null) {
            characteristics = this.featureDatasetCharacteristicService.findByDatasetId(datasetId);
        } else if (featureId != null) {
            characteristics = this.featureDatasetCharacteristicService.findByFeatureId(featureId);
        } else {
            characteristics = this.featureDatasetCharacteristicService.getAllFeatureDatasetCharacteristics();
        }

        List<FeatureDatasetCharacteristicDTO> dtos = characteristics.stream()
                .map(entity -> new FeatureDatasetCharacteristicDTO(entity))
                .collect(Collectors.toList());

        long totalCount = dtos.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(dtos);
    }

    /**
     * Create a new FeatureDatasetCharacteristic entity.
     * @param featureDatasetCharacteristicDTO the DTO containing data for the new FeatureDatasetCharacteristic with input structure
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createFeatureDatasetCharacteristic(@RequestBody FeatureDatasetCharacteristicDTO featureDatasetCharacteristicDTO,
                                                                @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try {

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            FeatureDatasetCharacteristic featureDatasetCharacteristic = new FeatureDatasetCharacteristic(featureDatasetCharacteristicDTO);
            FeatureDatasetCharacteristic savedFeatureDatasetCharacteristic = this.featureDatasetCharacteristicService.saveFeatureDatasetCharacteristic(featureDatasetCharacteristic);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedFeatureDatasetCharacteristic);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update FeatureDatasetCharacteristic using query parameters.
     * @param datasetId ID of the Dataset
     * @param featureId ID of the Feature
     * @param updatedFeatureDatasetCharacteristic FeatureDatasetCharacteristic model instance with updated details.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PutMapping()
    public ResponseEntity<?> updateFeatureDatasetCharacteristic(
            @RequestParam Long datasetId,
            @RequestParam Long featureId,
            @RequestBody FeatureDatasetCharacteristic updatedFeatureDatasetCharacteristic,
            @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        FeatureDatasetCharacteristicId featureDatasetCharacteristicId = new FeatureDatasetCharacteristicId();
        featureDatasetCharacteristicId.setDatasetId(datasetId);
        featureDatasetCharacteristicId.setFeatureId(featureId);

        try {

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<FeatureDatasetCharacteristic> savedFeatureDatasetCharacteristic = this.featureDatasetCharacteristicService.updateFeatureDatasetCharacteristic(featureDatasetCharacteristicId, updatedFeatureDatasetCharacteristic);
            if (savedFeatureDatasetCharacteristic.isPresent()) {
                return ResponseEntity.ok().body(savedFeatureDatasetCharacteristic);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by FeatureDatasetCharacteristic composite ID using query parameters.
     * @param datasetId ID of the Dataset
     * @param featureId ID of the Feature
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @DeleteMapping()
    public ResponseEntity<?> deleteFeatureDatasetCharacteristic(
            @RequestParam Long datasetId,
            @RequestParam Long featureId,
            @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        FeatureDatasetCharacteristicId featureDatasetCharacteristicId = new FeatureDatasetCharacteristicId();
        featureDatasetCharacteristicId.setDatasetId(datasetId);
        featureDatasetCharacteristicId.setFeatureId(featureId);

        try {

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.featureDatasetCharacteristicService.deleteFeatureDatasetCharacteristic(featureDatasetCharacteristicId);
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

