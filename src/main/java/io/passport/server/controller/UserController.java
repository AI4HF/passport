package io.passport.server.controller;

import io.passport.server.config.KeycloakProvider;
import io.passport.server.model.LoginRequest;
import org.keycloak.admin.client.Keycloak;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;

/**
 * Spring Controller which implements the Authentication measures and role management
 * Internally uses Keycloak's own API for the Authentication
 * Class of REST methods that run on http://localhost:8080/user
 */
@RestController
@RequestMapping("/user")
public class UserController {
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(UserController.class);
    private final KeycloakProvider kcProvider;


    public UserController(KeycloakProvider kcProvider) {
        this.kcProvider = kcProvider;
    }

    /**
     * Main login method which handles the Keycloak Authentication process and access token retrieval.
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@NotNull @RequestBody LoginRequest loginRequest) {
        try {
            Keycloak keycloak = kcProvider.newKeycloakBuilderWithPasswordCredentials(loginRequest.getUsername(), loginRequest.getPassword());
            String accessToken = keycloak.tokenManager().getAccessToken().getToken();
            return ResponseEntity.status(HttpStatus.OK).body(accessToken);
        } catch (BadRequestException ex) {
            LOG.warn("invalid account creds.", ex);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }
}