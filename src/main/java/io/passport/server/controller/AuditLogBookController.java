package io.passport.server.controller;

import io.passport.server.model.AuditLogBook;
import io.passport.server.model.AuditLog;
import io.passport.server.service.AuditLogBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/audit-log-book")
public class AuditLogBookController {

    private final AuditLogBookService auditLogBookService;

    @Autowired
    public AuditLogBookController(AuditLogBookService auditLogBookService) {
        this.auditLogBookService = auditLogBookService;
    }

    @GetMapping("/{passportId}")
    public List<AuditLogBook> getAuditLogsByPassportId(@PathVariable Long passportId,
                                                       @AuthenticationPrincipal Jwt principal) {
        return auditLogBookService.getAuditLogBooksByPassportId(passportId);
    }

    @PostMapping("")
    public void createAuditLogBookEntries(
            @RequestParam Long passportId,
            @RequestParam Long studyId,
            @RequestParam Long deploymentId,
            @AuthenticationPrincipal Jwt principal
    ) {
        auditLogBookService.createAuditLogBookEntries(passportId, studyId, deploymentId);
    }

    @PostMapping("/audit-logs")
    public List<AuditLog> getAuditLogsByIds(@RequestBody List<String> auditLogIds,
                                            @AuthenticationPrincipal Jwt principal) {
        return auditLogBookService.getAuditLogsByIds(auditLogIds);
    }
}
