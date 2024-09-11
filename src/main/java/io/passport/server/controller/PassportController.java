package io.passport.server.controller;

import io.passport.server.model.*;
import io.passport.server.service.*;
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

/**
 * Class which stores the generated HTTP requests related to passport operations.
 */
@RestController
@RequestMapping("/passport")
public class PassportController {

    private static final Logger log = LoggerFactory.getLogger(PassportController.class);
    /**
     * Passport service for passport management
     */
    private final PassportService passportService;

    /**
     * Role checker service for authorization
     */
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public PassportController(PassportService passportService, RoleCheckerService roleCheckerService) {
        this.passportService = passportService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read all passports
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<Passport>> getAllPassportsByStudyId(@RequestParam Long studyId, @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> allowedRoles = List.of(Role.QUALITY_ASSURANCE_SPECIALIST);
        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Passport> passports = this.passportService.findPassportsByStudyId(studyId);

        long totalCount = passports.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(passports);
    }

    /**
     * Delete passport by passportID.
     * @param passportId ID of the passport that is to be deleted.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @DeleteMapping("/{passportId}")
    public ResponseEntity<?> deletePassport(@PathVariable Long passportId,
                                            @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.QUALITY_ASSURANCE_SPECIALIST);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.passportService.deletePassport(passportId);
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

    /**
     * Endpoint to create a Passport and populate detailsJson field.
     *
     * @param passport The passport object with basic info (deploymentId, studyId, etc.).
     * @return The created Passport.
     */
    @PostMapping
    public ResponseEntity<Passport> createPassport(@RequestBody Passport passport) {
        Passport savedPassport = passportService.createPassport(passport);
        return ResponseEntity.status(201).body(savedPassport);
    }

    /**
     * Endpoint to retrieve Passport by passportId.
     *
     * @param passportId The ID of the passport.
     * @return The Passport object.
     */
    @GetMapping("/{passportId}")
    public ResponseEntity<Passport> getPassport(@PathVariable Long passportId) {
        Passport passport = passportService.getPassportById(passportId);
        return ResponseEntity.ok(passport);
    }


}
