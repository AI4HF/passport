package io.passport.server.controller;

import io.passport.server.model.Dataset;
import io.passport.server.model.Role;
import io.passport.server.service.DatasetService;
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

/**
 * Class which stores the generated HTTP requests related to Dataset operations.
 */
@RestController
@RequestMapping("/dataset")
public class DatasetController {
    private static final Logger log = LoggerFactory.getLogger(DatasetController.class);

    /**
     * Dataset service for Dataset management
     */
    private final DatasetService datasetService;

    /**
     * Role checker service for authorization
     */
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public DatasetController(DatasetService datasetService, RoleCheckerService roleCheckerService) {
        this.datasetService = datasetService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read all Datasets
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<Dataset>> getAllDatasets(@AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> allowedRoles = List.of(Role.DATA_ENGINEER, Role.DATA_SCIENTIST);
        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Dataset> datasets = this.datasetService.getAllDatasets();

        long totalCount = datasets.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(datasets);
    }

    /**
     * Read a Dataset by id
     * @param datasetId ID of the Dataset
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping("/{datasetId}")
    public ResponseEntity<?> getDataset(@PathVariable Long datasetId,
                                        @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> allowedRoles = List.of(Role.DATA_ENGINEER, Role.DATA_SCIENTIST);
        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Dataset> dataset = this.datasetService.findDatasetByDatasetId(datasetId);

        if(dataset.isPresent()) {
            return ResponseEntity.ok().body(dataset.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create Dataset.
     * @param dataset Dataset model instance to be created.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createDataset(@RequestBody Dataset dataset,
                                           @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Dataset savedDataset = this.datasetService.saveDataset(dataset);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDataset);
        } catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Dataset.
     * @param datasetId ID of the Dataset that is to be updated.
     * @param updatedDataset Dataset model instance with updated details.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PutMapping("/{datasetId}")
    public ResponseEntity<?> updateDataset(@PathVariable Long datasetId,
                                           @RequestBody Dataset updatedDataset,
                                           @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<Dataset> savedDataset = this.datasetService.updateDataset(datasetId, updatedDataset);
            if(savedDataset.isPresent()) {
                return ResponseEntity.ok().body(savedDataset);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by Dataset ID.
     * @param datasetId ID of the Dataset that is to be deleted.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @DeleteMapping("/{datasetId}")
    public ResponseEntity<?> deleteDataset(@PathVariable Long datasetId,
                                           @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.DATA_ENGINEER);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.datasetService.deleteDataset(datasetId);
            if(isDeleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
