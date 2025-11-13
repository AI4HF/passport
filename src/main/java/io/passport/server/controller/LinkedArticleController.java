package io.passport.server.controller;

import io.passport.server.model.Operation;
import io.passport.server.model.Role;
import io.passport.server.model.LinkedArticle;
import io.passport.server.model.TokenClaim;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.RoleCheckerService;
import io.passport.server.service.LinkedArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HTTP endpoints for Linked Article operations.
 */
@RestController
@RequestMapping("/linkedArticle")
public class LinkedArticleController {

    private static final Logger log = LoggerFactory.getLogger(LinkedArticleController.class);

    private final LinkedArticleService linkedArticleService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    // Allowed roles for access
    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER, Role.STUDY_OWNER, Role.DATA_SCIENTIST);

    // Used in audit logs
    private final String relationName = "Linked Article";

    public LinkedArticleController(LinkedArticleService linkedArticleService,
                                   RoleCheckerService roleCheckerService,
                                   AuditLogBookService auditLogBookService) {
        this.linkedArticleService = linkedArticleService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Read all LinkedArticles by studyId.
     * @param studyId study ID
     * @param principal JWT principal
     * @return list of LinkedArticles or 403 if unauthorized
     */
    @GetMapping
    public ResponseEntity<List<LinkedArticle>> getLinkedArticlesByStudyId(@RequestParam("studyId") String studyId,
                                                                          @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedToViewStudy(studyId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<LinkedArticle> results = this.linkedArticleService.findByStudyId(studyId);
        return ResponseEntity.ok(results);
    }

    /**
     * Create LinkedArticles for a study in bulk.
     * Body: JSON array of { articleUrl } objects.
     */
    @PostMapping
    public ResponseEntity<?> createLinkedArticles(@RequestParam String studyId,
                                                  @RequestBody List<LinkedArticle> articles,
                                                  @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<LinkedArticle> saved = this.linkedArticleService.replaceLinkedArticles(studyId, articles);

            // Create audit logs for each created record
            for (LinkedArticle sa : saved) {
                if (sa.getLinkedArticleId() != null) {
                    String recordId = sa.getLinkedArticleId();
                    auditLogBookService.createAuditLog(
                            principal.getSubject(),
                            principal.getClaim(TokenClaim.USERNAME.getValue()),
                            studyId,
                            Operation.CREATE,
                            relationName,
                            recordId,
                            sa
                    );
                }
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("Error creating LinkedArticles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
