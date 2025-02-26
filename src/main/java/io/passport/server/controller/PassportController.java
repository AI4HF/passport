package io.passport.server.controller;

import io.passport.server.model.Passport;
import io.passport.server.model.PassportWithDetailSelection;
import io.passport.server.model.Role;
import io.passport.server.service.PassportService;
import io.passport.server.service.PassportSignatureService;
import io.passport.server.service.RoleCheckerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Class which stores the generated HTTP requests related to passport operations.
 */
@RestController
@RequestMapping("/passport")
public class PassportController {

    private static final Logger log = LoggerFactory.getLogger(PassportController.class);
    private final PassportService passportService;
    private final RoleCheckerService roleCheckerService;
    private final PassportSignatureService passportSignatureService;
    private final List<Role> allowedRoles = List.of(Role.QUALITY_ASSURANCE_SPECIALIST);

    @Autowired
    public PassportController(PassportService passportService, RoleCheckerService roleCheckerService, PassportSignatureService passportSignatureService) {
        this.passportService = passportService;
        this.roleCheckerService = roleCheckerService;
        this.passportSignatureService = passportSignatureService;
    }

    /**
     * Read all passports by studyId.
     * @param studyId ID of the study
     * @param principal KeycloakPrincipal object that holds access token
     * @return List of Passports
     */
    @GetMapping()
    public ResponseEntity<List<Passport>> getAllPassportsByStudyId(@RequestParam String studyId,
                                                                   @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Passport> passports = this.passportService.findPassportsByStudyId(studyId);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(passports.size()));

        return ResponseEntity.ok().headers(headers).body(passports);
    }

    /**
     * Delete passport by passportId.
     * @param passportId ID of the passport that is to be deleted
     * @param studyId ID of the study for authorization
     * @param principal KeycloakPrincipal object that holds access token
     * @return No content or not found status
     */
    @DeleteMapping("/{passportId}")
    public ResponseEntity<?> deletePassport(@PathVariable String passportId,
                                            @RequestParam String studyId,
                                            @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.passportService.deletePassport(passportId);
            return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Create a Passport.
     * @param passport The passport object with basic info (deploymentId, studyId, etc.)
     * @param studyId ID of the study for authorization
     * @param principal KeycloakPrincipal object that holds access token
     * @return Created Passport
     */
    @PostMapping
    public ResponseEntity<?> createPassport(@RequestBody PassportWithDetailSelection passport,
                                            @RequestParam String studyId,
                                            @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Passport savedPassport = passportService.createPassport(passport);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPassport);
        } catch (RuntimeException e) {
            log.error("Error while creating passport: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while creating passport: " + e.getMessage());
        }
    }

    /**
     * Retrieve Passport by passportId.
     * @param passportId The ID of the passport
     * @param studyId ID of the study for authorization
     * @param principal KeycloakPrincipal object that holds access token
     * @return Passport object
     */
    @GetMapping("/{passportId}")
    public ResponseEntity<Passport> getPassport(@PathVariable String passportId,
                                                @RequestParam String studyId,
                                                @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Passport passport = passportService.getPassportById(passportId);
        return ResponseEntity.ok(passport);
    }

    /**
     * New endpoint to sign a PDF file.
     *
     * Receives a PDF as a multipart file, checks authorization,
     * then signs it using the PassportSignatureService.
     *
     * @param file       The incoming PDF file to be signed
     * @param studyId    Study ID for authorization
     * @param principal  Keycloak JWT principal
     * @return           A ResponseEntity containing the signed PDF in bytes
     */
    @PostMapping(value = "/sign-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> signPdf(@RequestParam("pdf") MultipartFile file,
                                     @RequestParam String studyId,
                                     @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            byte[] fileBytes = file.getBytes();

            byte[] signedPdf = passportSignatureService.generateSignature(fileBytes);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);

            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("signed_document.pdf")
                    .build());

            return new ResponseEntity<>(signedPdf, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error while signing PDF: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not sign the PDF: " + e.getMessage());
        }
    }
}
