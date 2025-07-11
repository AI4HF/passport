package io.passport.server.controller;

import io.passport.server.model.Credentials;
import io.passport.server.service.KeycloakService;
import jakarta.ws.rs.NotAuthorizedException;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



/**
 * Keycloak authorization tools and request.
 */
@RestController
@RequestMapping("/user")
public class KeycloakController {

    /**
     * Keycloak service for keycloak management
     */
    private KeycloakService keycloakService;

    @Autowired
    public KeycloakController(KeycloakService keycloakService) {
        this.keycloakService = keycloakService;
    }

    /**
     * Login request which sends the necessary credentials to Keycloak along with environment variables, and returns the authentication token.
     * @param user User credentials.
     * @return Token
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Credentials user) {
        try {
            AccessTokenResponse loginResponse = keycloakService.getAccessToken(user.username, user.password);

            return ResponseEntity.ok(loginResponse);
        } catch (NotAuthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing credentials.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
        }
    }

    /**
     * Refresh Token retrieval request with credentials
     * @param creds User credentials
     * @return A response with the Refresh Token
     */
    @PostMapping("/connector/sign-up")
    public ResponseEntity<?> createConnectorSecret(@RequestBody Credentials creds) {
        try {
            String token = keycloakService.createOfflineSecret(creds.username, creds.password);
            return ResponseEntity.ok(token);
        } catch (NotAuthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials.");
        }
    }

    /**
     * Refresh Token redeeming request with the pre-provided refresh token.
     * @param refreshToken Refresh Token which acts as a connector secret
     * @return A response with the Access Token
     */
    @PostMapping("/connector/login")
    public ResponseEntity<?> loginWithSecret(@RequestBody String refreshToken) {
        try {
            AccessTokenResponse tokens = keycloakService.refreshWithSecret(refreshToken);
            return ResponseEntity.ok(tokens);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Secret isn't valid.");
        }
    }
}
