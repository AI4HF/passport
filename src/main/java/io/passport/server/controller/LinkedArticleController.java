package io.passport.server.controller;

import io.passport.server.model.LinkedArticle;
import io.passport.server.model.Operation;
import io.passport.server.model.Role;
import io.passport.server.model.TokenClaim;
import io.passport.server.service.AuditLogBookService;
import io.passport.server.service.LinkedArticleService;
import io.passport.server.service.RoleCheckerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Class which stores the generated HTTP requests related to linked article operations.
 */
@RestController
@RequestMapping("/linked-article")
public class LinkedArticleController {

    private static final Logger log = LoggerFactory.getLogger(LinkedArticleController.class);

    private final String relationName = "Linked Article";
    private final LinkedArticleService linkedArticleService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;

    private final List<Role> allowedRoles = List.of(Role.STUDY_OWNER, Role.DATA_ENGINEER);

    @Autowired
    public LinkedArticleController(LinkedArticleService linkedArticleService,
                                   RoleCheckerService roleCheckerService,
                                   AuditLogBookService auditLogBookService) {
        this.linkedArticleService = linkedArticleService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    /**
     * Read linked article by linkedArticleId.
     *
     * @param linkedArticleId ID of the linked article.
     * @param studyId         ID of the study.
     * @param principal       Jwt principal containing user info.
     * @return ResponseEntity with the linked article data.
     */
    @GetMapping("/{linkedArticleId}")
    public ResponseEntity<?> getLinkedArticleById(@PathVariable("linkedArticleId") String linkedArticleId,
                                                  @RequestParam String studyId,
                                                  @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedToViewStudy(studyId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<LinkedArticle> article = this.linkedArticleService.findLinkedArticleById(linkedArticleId);
        return article.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Read linked articles by studyId.
     *
     * @param studyId   ID of the study.
     * @param principal Jwt principal containing user info.
     * @return ResponseEntity with the list of linked articles.
     */
    @GetMapping
    public ResponseEntity<?> getLinkedArticlesByStudyId(@RequestParam String studyId,
                                                        @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedToViewStudy(studyId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<LinkedArticle> articles = this.linkedArticleService.findLinkedArticleByStudyId(studyId);
        return ResponseEntity.ok().body(articles);
    }

    /**
     * Create Linked Article.
     * (Only STUDY_OWNER is allowed to create)
     *
     * @param linkedArticle LinkedArticle model instance to be created.
     * @param studyId       ID of the study.
     * @param principal     Jwt principal containing user info.
     * @return ResponseEntity with the created linked article data.
     */
    @PostMapping
    public ResponseEntity<?> createLinkedArticle(@RequestBody LinkedArticle linkedArticle,
                                                 @RequestParam String studyId,
                                                 @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            LinkedArticle savedArticle = this.linkedArticleService.saveLinkedArticle(linkedArticle);

            if (savedArticle.getLinkedArticleId() != null) {
                String recordId = savedArticle.getLinkedArticleId();
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.CREATE,
                        relationName,
                        recordId,
                        savedArticle
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(savedArticle);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Linked Article.
     * (Only STUDY_OWNER is allowed to update)
     *
     * @param linkedArticleId      ID of the linked article to be updated.
     * @param updatedLinkedArticle Updated linked article model.
     * @param studyId              ID of the study.
     * @param principal            Jwt principal containing user info.
     * @return ResponseEntity with the updated linked article data.
     */
    @PutMapping("/{linkedArticleId}")
    public ResponseEntity<?> updateLinkedArticle(@PathVariable String linkedArticleId,
                                                 @RequestBody LinkedArticle updatedLinkedArticle,
                                                 @RequestParam String studyId,
                                                 @AuthenticationPrincipal Jwt principal) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Optional<LinkedArticle> savedArticleOpt =
                    this.linkedArticleService.updateLinkedArticle(linkedArticleId, updatedLinkedArticle);

            if (savedArticleOpt.isPresent()) {
                LinkedArticle savedArticle = savedArticleOpt.get();
                String recordId = savedArticle.getLinkedArticleId();
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.UPDATE,
                        relationName,
                        recordId,
                        savedArticle
                );
                return ResponseEntity.ok(savedArticle);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete Linked Article by linkedArticleId.
     * (Only STUDY_OWNER is allowed to delete)
     *
     * @param linkedArticleId ID of the linked article to be deleted.
     * @param studyId         ID of the study.
     * @param principal       Jwt principal containing user info.
     * @return ResponseEntity with no content if successful.
     */
    @DeleteMapping("/{linkedArticleId}")
    public ResponseEntity<?> deleteLinkedArticle(@PathVariable String linkedArticleId,
                                                 @RequestParam String studyId,
                                                 @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Optional<LinkedArticle> deletedArticle = this.linkedArticleService.deleteLinkedArticle(linkedArticleId);
            if (deletedArticle.isPresent()) {
                auditLogBookService.createAuditLog(
                        principal.getSubject(),
                        principal.getClaim(TokenClaim.USERNAME.getValue()),
                        studyId,
                        Operation.DELETE,
                        relationName,
                        linkedArticleId,
                        deletedArticle.get()
                );
                return ResponseEntity.status(HttpStatus.OK).body(deletedArticle.get());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
