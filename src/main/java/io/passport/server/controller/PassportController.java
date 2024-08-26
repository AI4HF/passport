package io.passport.server.controller;

import io.passport.server.model.Passport;
import io.passport.server.model.Role;
import io.passport.server.service.PassportService;
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
     * Read a passport by passportId
     * @param passportId ID of the passport
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping("/{passportId}")
    public ResponseEntity<?> getPassport(@PathVariable Long passportId,
                                         @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> allowedRoles = List.of(Role.QUALITY_ASSURANCE_SPECIALIST);
        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Passport> passport = this.passportService.findPassportByPassportId(passportId);

        if(passport.isPresent()) {
            return ResponseEntity.ok().body(passport.get());
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create Passport.
     * @param passport Passport model instance to be created.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createPassport(@RequestBody Passport passport,
                                            @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.QUALITY_ASSURANCE_SPECIALIST);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Passport savedPassport = this.passportService.savePassport(passport);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPassport);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
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
}
