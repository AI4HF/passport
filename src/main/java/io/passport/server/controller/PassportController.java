package io.passport.server.controller;

import io.passport.server.model.Passport;
import io.passport.server.model.PassportWithDetailSelection;
import io.passport.server.model.PdfRequest;
import io.passport.server.model.Role;
import io.passport.server.service.PassportService;
import io.passport.server.service.PassportSignatureService;
import io.passport.server.service.PdfRenderService;
import io.passport.server.service.RoleCheckerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Class which stores the generated HTTP requests related to passport operations.
 */
@RestController
@RequestMapping("/passport")
public class PassportController {

    private static final Logger log = LoggerFactory.getLogger(PassportController.class);
    private final PdfRenderService renderer;
    private final PassportService passportService;
    private final RoleCheckerService roleCheckerService;
    private final PassportSignatureService passportSignatureService;
    private final List<Role> allowedRoles = List.of(Role.QUALITY_ASSURANCE_SPECIALIST);

    @Autowired
    public PassportController(PassportService passportService, RoleCheckerService roleCheckerService, PassportSignatureService passportSignatureService, PdfRenderService renderer) {
        this.passportService = passportService;
        this.roleCheckerService = roleCheckerService;
        this.passportSignatureService = passportSignatureService;
        this.renderer = renderer;
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
     * Combined request to generate a passport PDF from a HTML, then sign it.
     *
     * @param req PDF generation request DTO
     * @param principal Keycloak JWT principal
     * @return A ResponseEntity containing the signed PDF in bytes
     */
    @PostMapping(value = "/generate-and-sign", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> generateAndSign(@RequestBody PdfRequest req,
                                             @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(req.getStudyId(), principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (req.getHtmlContent() == null || req.getHtmlContent().isBlank()) {
                return ResponseEntity.badRequest().body("Missing htmlContent");
            }
            if (req.getStudyId() == null || req.getStudyId().isBlank()) {
                return ResponseEntity.badRequest().body("Missing studyId");
            }

            byte[] pdf = renderer.render(
                    req.getHtmlContent(),
                    req.getBaseUrl(),
                    (req.getWidth() != null && !req.getWidth().isBlank()) ? req.getWidth() : "420mm",
                    (req.getHeight() != null && !req.getHeight().isBlank()) ? req.getHeight() : "297mm",
                    (req.getLandscape() != null) ? req.getLandscape() : Boolean.TRUE
            );

            byte[] signed = passportSignatureService.generateSignature(pdf);

            String outName = req.getFileName();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment().filename(outName).build());
            headers.add("Access-Control-Expose-Headers", "Content-Disposition");
            headers.setContentLength(signed.length);

            return new ResponseEntity<>(signed, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate and sign PDF: " + e.getMessage());
        }
    }
}
