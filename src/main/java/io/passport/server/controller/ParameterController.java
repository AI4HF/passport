package io.passport.server.controller;

import io.passport.server.model.Parameter;
import io.passport.server.model.Role;
import io.passport.server.service.ParameterService;
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
 * Class which stores the generated HTTP requests related to parameter. operations.
 */
@RestController
@RequestMapping("/parameter")
public class ParameterController {

    private static final Logger log = LoggerFactory.getLogger(ParameterController.class);

    /**
     * Parameter service for parameter management.
     */
    private final ParameterService parameterService;

    /**
     * Role checker service for authorization
     */
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public ParameterController(ParameterService parameterService, RoleCheckerService roleCheckerService) {
        this.parameterService = parameterService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read all parameters by StudyId
     * @param studyId ID of the study
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<Parameter>> getAllParametersByStudyId(@RequestParam Long studyId,
                                                            @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST, Role.QUALITY_ASSURANCE_SPECIALIST);
        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Parameter> parameters = this.parameterService.findParametersByStudyId(studyId);

        long totalCount = parameters.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(parameters);
    }

    /**
     * Read a parameter by id
     * @param parameterId ID of the parameter
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping("/{parameterId}")
    public ResponseEntity<?> getParameterById(@PathVariable Long parameterId,
                                              @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Parameter> parameter = this.parameterService.findParameterById(parameterId);

        if (parameter.isPresent()) {
            return ResponseEntity.ok().body(parameter);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * Create a Parameter.
     * @param parameter parameter model instance to be created.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createParameter(@RequestBody Parameter parameter,
                                             @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Parameter savedParameter = this.parameterService.saveParameter(parameter);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedParameter);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Parameter.
     * @param parameterId ID of the parameter that is to be updated.
     * @param updatedParameter model instance with updated details.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PutMapping("/{parameterId}")
    public ResponseEntity<?> updateParameter(@PathVariable Long parameterId,
                                             @RequestBody Parameter updatedParameter,
                                             @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<Parameter> savedParameter = this.parameterService.updateParameter(parameterId, updatedParameter);
            if (savedParameter.isPresent()) {
                return ResponseEntity.ok().body(savedParameter.get());
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete a parameter by Parameter ID.
     * @param parameterId ID of the parameter that is to be deleted.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @DeleteMapping("/{parameterId}")
    public ResponseEntity<?> deleteParameter(@PathVariable Long parameterId,
                                             @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.parameterService.deleteParameter(parameterId);
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
