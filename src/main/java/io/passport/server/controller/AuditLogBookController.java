package io.passport.server.controller;

import io.passport.server.model.AuditLogBook;
import io.passport.server.model.AuditLog;
import io.passport.server.model.Role;
import io.passport.server.service.AuditLogBookService;
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

/**
 * Class which stores the generated HTTP requests related to Audit Log Book access.
 */
@RestController
@RequestMapping("/audit-log-book")
public class AuditLogBookController {

    private static final Logger log = LoggerFactory.getLogger(AuditLogBookController.class);

    private final AuditLogBookService auditLogBookService;
    private final RoleCheckerService roleCheckerService;
    private final List<Role> allowedRoles = List.of(Role.QUALITY_ASSURANCE_SPECIALIST);

    @Autowired
    public AuditLogBookController(AuditLogBookService auditLogBookService, RoleCheckerService roleCheckerService) {
        this.auditLogBookService = auditLogBookService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Retrieves all Audit Logs for the given Passport.
     * @param passportId Id of the related Passport.
     * @param studyId Connected Study's id.
     * @param principal JWT principal containing user info.
     * @return
     */
    @GetMapping("/{passportId}")
    public ResponseEntity<?> getAuditLogsByPassportId(@PathVariable String passportId,
                                                      @RequestParam String studyId,
                                                      @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<AuditLogBook> logBooks = auditLogBookService.getAuditLogBooksByPassportId(passportId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(logBooks.size()));
        return ResponseEntity.ok().headers(headers).body(logBooks);
    }

    /**
     * Kept for clients that still call it after creating a passport; it changes nothing. A passport's audit log
     * book is the snapshot taken when the passport is created (in the same transaction), and it never changes
     * afterwards - not even when it is empty, or when the passport was created before books were taken this way.
     * @param passportId Passport related to all of Audit Log Books.
     * @param studyId Connected Study of all the Audit Logs.
     * @param principal JWT principal containing user info.
     * @return 201, as before, so existing clients keep working
     */
    @PostMapping("")
    public ResponseEntity<?> createAuditLogBookEntries(
            @RequestParam String passportId,
            @RequestParam String studyId,
            @AuthenticationPrincipal Jwt principal
    ) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Retrieves all Audit Logs that match the given list of Audit Log ids.
     * @param auditLogIds List of requested Audit Log ids.
     * @param studyId Connected study id which is used for authorization purposes.
     * @param principal JWT principal containing user info.
     * @return
     */
    @PostMapping("/audit-logs")
    public ResponseEntity<?> getAuditLogsByIds(@RequestBody List<String> auditLogIds,
                                               @RequestParam String studyId,
                                               @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<AuditLog> logs = auditLogBookService.getAuditLogsByIds(auditLogIds);
        return ResponseEntity.ok().body(logs);
    }
}
