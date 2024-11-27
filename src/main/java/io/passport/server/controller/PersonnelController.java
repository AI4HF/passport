package io.passport.server.controller;

import io.passport.server.model.Personnel;
import io.passport.server.model.PersonnelDTO;
import io.passport.server.model.Role;
import io.passport.server.service.PersonnelService;
import io.passport.server.service.RoleCheckerService;
import org.keycloak.KeycloakPrincipal;
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
 * Class which stores the generated HTTP requests related to personnel operations.
 */
@RestController
@RequestMapping("/personnel")
public class PersonnelController {

    private static final Logger log = LoggerFactory.getLogger(PersonnelController.class);

    /**
     * Personnel repo access for database management.
     */
    private final PersonnelService personnelService;

    /**
     * Role checker service for authorization
     */
    private final RoleCheckerService roleCheckerService;

    /**
     * List of authorized roles for this endpoint
     */
    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER, Role.DATA_SCIENTIST, Role.ML_ENGINEER,
            Role.ORGANIZATION_ADMIN, Role.QUALITY_ASSURANCE_SPECIALIST, Role.STUDY_OWNER, Role.SURVEY_MANAGER);

    @Autowired
    public PersonnelController(PersonnelService personnelService, RoleCheckerService roleCheckerService) {
        this.personnelService = personnelService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * If organizationId exists, get all personnel related to this organizationId otherwise get all personnel.
     * @param organizationId ID of the organization related to personnel.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<Personnel>> getPersonnelByOrganizationId(@RequestParam Optional<Long> organizationId,
                                                                        @AuthenticationPrincipal Jwt principal) {

        List<Personnel> personnel = organizationId
                .map(this.personnelService::findPersonnelByOrganizationId)
                .orElseGet(this.personnelService::getAllPersonnel);


        long totalCount = personnel.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(personnel);
    }

    /**
     * Read personnel by personId
     * @param personId ID of the personnel.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping("/{personId}")
    public ResponseEntity<?> getPersonnelByPersonId(
            @PathVariable("personId") String personId,
            @AuthenticationPrincipal Jwt principal) {

        Optional<Personnel> personnel = this.personnelService.findPersonnelById(personId);

        if(personnel.isPresent()) {
            return ResponseEntity.ok().body(personnel);
        }else{
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * Create a Personnel.
     * @param personnelDTO Personnel model instance to be created.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createPersonnel(@RequestBody PersonnelDTO personnelDTO,
                                             @AuthenticationPrincipal Jwt principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> lesserAllowedRoles = List.of(Role.ORGANIZATION_ADMIN);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, lesserAllowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<Personnel> savedPersonnel = this.personnelService.savePersonnel(personnelDTO);
            if(savedPersonnel.isPresent()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(savedPersonnel);
            }else{
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Personnel.
     * @param personId ID of the personnel that is to be updated.
     * @param updatedPersonnel Personnel model instance with updated details.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PutMapping("/{personId}")
    public ResponseEntity<?> updatePersonnel(@PathVariable String personId,
                                             @RequestBody Personnel updatedPersonnel,
                                             @AuthenticationPrincipal Jwt principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> lesserAllowedRoles = List.of(Role.ORGANIZATION_ADMIN);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, lesserAllowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<Personnel> savedPersonnel = this.personnelService.updatePersonnel(personId, updatedPersonnel);
            if(savedPersonnel.isPresent()) {
                return ResponseEntity.ok().body(savedPersonnel.get());
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete a personnel by Personnel ID.
     * @param personId ID of the personnel that is to be deleted.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @DeleteMapping("/{personId}")
    public ResponseEntity<?> deletePersonnel(@PathVariable String personId ,
                                             @AuthenticationPrincipal Jwt principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> lesserAllowedRoles = List.of(Role.ORGANIZATION_ADMIN);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, lesserAllowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.personnelService.deletePersonnel(personId);
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