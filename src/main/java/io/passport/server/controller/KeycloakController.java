package io.passport.server.controller;

import io.passport.server.model.Credentials;
import io.passport.server.service.KeycloakService;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class KeycloakController {

    @Autowired
    private KeycloakService keycloakService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Credentials user) {
        try {
            Keycloak keycloak = keycloakService.newKeycloakBuilderWithPasswordCredentials(user.username, user.password);
            AccessTokenResponse tokenResponse = keycloak.tokenManager().grantToken();

            return ResponseEntity.ok(tokenResponse);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }
}
