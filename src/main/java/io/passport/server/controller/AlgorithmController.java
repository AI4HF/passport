package io.passport.server.controller;

import io.passport.server.model.*;
import io.passport.server.service.AlgorithmService;
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
import java.util.Optional;

/**
 * Class which stores the generated HTTP requests related to Algorithm operations.
 */
@RestController
@RequestMapping("/algorithm")
public class AlgorithmController {

    private static final Logger log = LoggerFactory.getLogger(AlgorithmController.class);

    private final String relationName = "Algorithm";
    private final AlgorithmService algorithmService;
    private final RoleCheckerService roleCheckerService;
    private final AuditLogBookService auditLogBookService;
    private final List<Role> allowedRoles = List.of(Role.DATA_SCIENTIST);

    @Autowired
    public AlgorithmController(
            AlgorithmService algorithmService,
            RoleCheckerService roleCheckerService,
            AuditLogBookService auditLogBookService
    ) {
        this.algorithmService = algorithmService;
        this.roleCheckerService = roleCheckerService;
        this.auditLogBookService = auditLogBookService;
    }

    @GetMapping
    public ResponseEntity<List<Algorithm>> getAllAlgorithms(
            @RequestParam Long studyId,
            @AuthenticationPrincipal Jwt principal
    ) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Algorithm> algorithms = this.algorithmService.getAllAlgorithms();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(algorithms.size()));
        return ResponseEntity.ok().headers(headers).body(algorithms);
    }

    @GetMapping("/{algorithmId}")
    public ResponseEntity<?> getAlgorithm(
            @PathVariable Long algorithmId,
            @RequestParam Long studyId,
            @AuthenticationPrincipal Jwt principal
    ) {
        if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Algorithm> algorithm = this.algorithmService.findAlgorithmById(algorithmId);
        return algorithm.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createAlgorithm(
            @RequestBody Algorithm algorithm,
            @RequestParam Long studyId,
            @AuthenticationPrincipal Jwt principal
    ) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Algorithm savedAlgorithm = this.algorithmService.saveAlgorithm(algorithm);
            if (savedAlgorithm.getAlgorithmId() == null) {
                log.error("Error creating the algorithm: {}", algorithm);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            String recordId = String.valueOf(savedAlgorithm.getAlgorithmId());
            auditLogBookService.createAuditLog(
                    principal.getSubject(),
                    principal.getClaim(TokenClaim.USERNAME.getValue()),
                    studyId,
                    Operation.CREATE,
                    relationName,
                    recordId,
                    savedAlgorithm
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(savedAlgorithm);

        } catch (Exception e) {
            log.error("Error creating Algorithm: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{algorithmId}")
    public ResponseEntity<?> updateAlgorithm(
            @PathVariable Long algorithmId,
            @RequestBody Algorithm updatedAlgorithm,
            @RequestParam Long studyId,
            @AuthenticationPrincipal Jwt principal
    ) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<Algorithm> savedAlgorithmOpt = this.algorithmService.updateAlgorithm(algorithmId, updatedAlgorithm);
            if (savedAlgorithmOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Algorithm savedAlgorithm = savedAlgorithmOpt.get();

            String recordId = String.valueOf(savedAlgorithm.getAlgorithmId());
            auditLogBookService.createAuditLog(
                    principal.getSubject(),
                    principal.getClaim(TokenClaim.USERNAME.getValue()),
                    studyId,
                    Operation.UPDATE,
                    relationName,
                    recordId,
                    savedAlgorithm
            );

            return ResponseEntity.ok(savedAlgorithm);

        } catch (Exception e) {
            log.error("Error updating Algorithm: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{algorithmId}")
    public ResponseEntity<?> deleteAlgorithm(
            @PathVariable Long algorithmId,
            @RequestParam Long studyId,
            @AuthenticationPrincipal Jwt principal
    ) {
        try {
            if (!this.roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<Algorithm> deletedAlgorithm = this.algorithmService.deleteAlgorithm(algorithmId);
            if (deletedAlgorithm.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            String recordId = String.valueOf(algorithmId);
            auditLogBookService.createAuditLog(
                    principal.getSubject(),
                    principal.getClaim(TokenClaim.USERNAME.getValue()),
                    studyId,
                    Operation.DELETE,
                    relationName,
                    recordId,
                    deletedAlgorithm.get()
            );

            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(deletedAlgorithm.get());

        } catch (Exception e) {
            log.error("Error deleting Algorithm: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
