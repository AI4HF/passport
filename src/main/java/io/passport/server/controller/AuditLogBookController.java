package io.passport.server.controller;

import io.passport.server.model.AuditLogBook;
import io.passport.server.model.AuditLog;
import io.passport.server.service.AuditLogBookService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<AuditLogBook> getAuditLogsByPassportId(@PathVariable String passportId) {
        return auditLogBookService.getAuditLogBooksByPassportId(passportId);
    }

    @PostMapping("")
    public void createAuditLogBookEntries(
            @RequestParam String passportId,
            @RequestParam Long studyId,
            @RequestParam Long deploymentId
    ) {
        auditLogBookService.createAuditLogBookEntries(passportId, studyId, deploymentId);
    }

    @PostMapping("/audit-logs")
    public List<AuditLog> getAuditLogsByIds(@RequestBody List<String> auditLogIds) {
        return auditLogBookService.getAuditLogsByIds(auditLogIds);
    }
}
