package io.passport.server.controller;

import io.passport.server.model.Operation;
import io.passport.server.model.Role;
import io.passport.server.model.StaticArticle;
import io.passport.server.model.TokenClaim;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.RoleCheckerService;
import io.passport.server.service.StaticArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HTTP endpoints for Static Article operations.
 */
@RestController
@RequestMapping("/staticArticle")
public class StaticArticleController {

    private static final Logger log = LoggerFactory.getLogger(StaticArticleController.class);

    private final StaticArticleService staticArticleService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    // Allowed roles for access
    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER, Role.STUDY_OWNER, Role.DATA_SCIENTIST);

    // Used in audit logs
    private final String relationName = "Static Article";

    public StaticArticleController(StaticArticleService staticArticleService,
                                   RoleCheckerService roleCheckerService,
                                   AuditLogBookService auditLogBookService) {
        this.staticArticleService = staticArticleService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Read all StaticArticles by studyId.
     * @param studyId study ID
     * @param principal JWT principal
     * @return list of StaticArticles or 403 if unauthorized
     */
    @GetMapping
    public ResponseEntity<List<StaticArticle>> getStaticArticlesByStudyId(@RequestParam("studyId") String studyId,
                                                                          @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedToViewStudy(studyId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<StaticArticle> results = this.staticArticleService.findByStudyId(studyId);
        return ResponseEntity.ok(results);
    }

    /**
     * Create StaticArticles for a study in bulk.
     * Body: JSON array of { articleUrl } objects.
     */
    @PostMapping
    public ResponseEntity<?> createStaticArticles(@RequestParam String studyId,
                                                  @RequestBody List<StaticArticle> articles,
                                                  @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<StaticArticle> saved = this.staticArticleService.createStaticArticleEntries(studyId, articles);

            // Create audit logs for each created record
            for (StaticArticle sa : saved) {
                if (sa.getStaticArticleId() != null) {
                    String recordId = sa.getStaticArticleId();
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
            log.error("Error creating StaticArticles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
