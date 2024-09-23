package io.passport.server.controller;

import io.passport.server.model.Algorithm;
import io.passport.server.model.Role;
import io.passport.server.service.AlgorithmService;
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
 * Class which stores the generated HTTP requests related to algorithm operations.
 */
@RestController
@RequestMapping("/algorithm")
public class AlgorithmController {
    private static final Logger log = LoggerFactory.getLogger(AlgorithmController.class);
    /**
     * Algorithm service for algorithm management
     */
    private final AlgorithmService algorithmService;

    /**
     * Role checker service for authorization
     */
    private final RoleCheckerService roleCheckerService;

    /**
     * List of authorized roles for this endpoint
     */
    private final List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);

    @Autowired
    public AlgorithmController(AlgorithmService algorithmService, RoleCheckerService roleCheckerService) {
        this.algorithmService = algorithmService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read all algorithms
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<Algorithm>> getAllAlgorithms(@AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Algorithm> algorithms = this.algorithmService.getAllAlgorithms();

        long totalCount = algorithms.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(algorithms);
    }

    /**
     * Read an algorithm by id
     * @param algorithmId ID of the algorithm
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping("/{algorithmId}")
    public ResponseEntity<?> getAlgorithm(@PathVariable Long algorithmId,
                                          @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Algorithm> algorithm = this.algorithmService.findAlgorithmById(algorithmId);

        if(algorithm.isPresent()) {
            return ResponseEntity.ok().body(algorithm.get());
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create Algorithm.
     * @param algorithm Algorithm model instance to be created.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createAlgorithm(@RequestBody Algorithm algorithm,
                                             @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Algorithm savedAlgorithm = this.algorithmService.saveAlgorithm(algorithm);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAlgorithm);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Algorithm.
     * @param algorithmId ID of the algorithm that is to be updated.
     * @param updatedAlgorithm Algorithm model instance with updated details.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PutMapping("/{algorithmId}")
    public ResponseEntity<?> updateAlgorithm(@PathVariable Long algorithmId,
                                             @RequestBody Algorithm updatedAlgorithm,
                                             @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<Algorithm> savedAlgorithm = this.algorithmService.updateAlgorithm(algorithmId, updatedAlgorithm);
            if(savedAlgorithm.isPresent()) {
                return ResponseEntity.ok().body(savedAlgorithm);
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by Algorithm ID.
     * @param algorithmId ID of the algorithm that is to be deleted.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @DeleteMapping("/{algorithmId}")
    public ResponseEntity<?> deleteAlgorithm(@PathVariable Long algorithmId,
                                             @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.algorithmService.deleteAlgorithm(algorithmId);
            if(isDeleted) {
                return ResponseEntity.noContent().build();
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
