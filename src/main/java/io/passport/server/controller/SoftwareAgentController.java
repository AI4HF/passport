package io.passport.server.controller;

import io.passport.server.model.Role;
import io.passport.server.model.SoftwareAgent;
import io.passport.server.service.RoleCheckerService;
import io.passport.server.service.SoftwareAgentService;
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
 * Class which stores the generated HTTP requests related to software agent operations.
 * Software agents are realm-wide like organizations and personnel, so authorization is by
 * realm role rather than by study membership.
 */
@RestController
@RequestMapping("/software-agent")
public class SoftwareAgentController {

    private static final Logger log = LoggerFactory.getLogger(SoftwareAgentController.class);

    /**
     * SoftwareAgent service for software agent management.
     */
    private final SoftwareAgentService softwareAgentService;

    /**
     * Role checker service for authorization
     */
    private final RoleCheckerService roleCheckerService;

    /**
     * Roles allowed to read the software agent registry
     */
    private final List<Role> allowedRoles = List.of(Role.DATA_ENGINEER, Role.DATA_SCIENTIST, Role.DATA_STEWARD,
            Role.ORGANIZATION_ADMIN, Role.QUALITY_ASSURANCE_SPECIALIST, Role.STUDY_OWNER, Role.SURVEY_MANAGER);

    /**
     * Roles allowed to change it
     */
    private final List<Role> writeRoles = List.of(Role.ORGANIZATION_ADMIN);

    @Autowired
    public SoftwareAgentController(SoftwareAgentService softwareAgentService, RoleCheckerService roleCheckerService) {
        this.softwareAgentService = softwareAgentService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Read all software agents
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<SoftwareAgent>> getAllSoftwareAgents(@AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.hasAnyRole(principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<SoftwareAgent> softwareAgents = this.softwareAgentService.getAllSoftwareAgents();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(softwareAgents.size()));

        return ResponseEntity.ok().headers(headers).body(softwareAgents);
    }

    /**
     * Read a software agent by id
     * @param softwareAgentId ID of the software agent
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping("/{softwareAgentId}")
    public ResponseEntity<?> getSoftwareAgentById(@PathVariable String softwareAgentId,
                                                  @AuthenticationPrincipal Jwt principal) {

        if (!this.roleCheckerService.hasAnyRole(principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<SoftwareAgent> softwareAgent = this.softwareAgentService.findSoftwareAgentById(softwareAgentId);

        return softwareAgent.<ResponseEntity<?>>map(agent -> ResponseEntity.ok().body(agent))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create a SoftwareAgent.
     * @param softwareAgent SoftwareAgent model instance to be created.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createSoftwareAgent(@RequestBody SoftwareAgent softwareAgent,
                                                 @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.hasAnyRole(principal, writeRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            SoftwareAgent savedSoftwareAgent = this.softwareAgentService.saveSoftwareAgent(softwareAgent);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedSoftwareAgent);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update a SoftwareAgent.
     * @param softwareAgentId ID of the software agent to be updated
     * @param updatedSoftwareAgent SoftwareAgent model instance with updated details
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @PutMapping("/{softwareAgentId}")
    public ResponseEntity<?> updateSoftwareAgent(@PathVariable String softwareAgentId,
                                                 @RequestBody SoftwareAgent updatedSoftwareAgent,
                                                 @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.hasAnyRole(principal, writeRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<SoftwareAgent> savedSoftwareAgent =
                    this.softwareAgentService.updateSoftwareAgent(softwareAgentId, updatedSoftwareAgent);

            return savedSoftwareAgent.<ResponseEntity<?>>map(agent -> ResponseEntity.ok().body(agent))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete a SoftwareAgent.
     * @param softwareAgentId ID of the software agent to be deleted
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @DeleteMapping("/{softwareAgentId}")
    public ResponseEntity<?> deleteSoftwareAgent(@PathVariable String softwareAgentId,
                                                 @AuthenticationPrincipal Jwt principal) {
        try {
            if (!this.roleCheckerService.hasAnyRole(principal, writeRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.softwareAgentService.deleteSoftwareAgent(softwareAgentId);
            if (isDeleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
