package io.passport.server.controller;

import io.passport.server.model.Credentials;
import io.passport.server.service.KeycloakService;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.NotAuthorizedException;

/**
 * Keycloak authorization tools and request.
 */
@RestController
@RequestMapping("/user")
public class KeycloakController {

    @Autowired
    private KeycloakService keycloakService;

    /**
     * Login request which sends the necessary credentials to Keycloak along with environment variables, and returns the authentication token.
     * @param user User credentials.
     * @return Token
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Credentials user) {
        try {
            Keycloak keycloak = keycloakService.newKeycloakBuilderWithPasswordCredentials(user.username, user.password);
            AccessTokenResponse tokenResponse = keycloak.tokenManager().grantToken();

            return ResponseEntity.ok(tokenResponse);
        } catch (NotAuthorizedException e) {
            return ResponseEntity.status(401).body("Invalid credentials.");
        }
    }
}
