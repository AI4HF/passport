package io.passport.server.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.passport.server.config.KeycloakProvider;
import io.passport.server.model.LoginRequest;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
     * Our main login method
     * Handles the Keycloak Authentication process, access token and resource_access retrieval.
     * Extracts the user's roles on the client and returns them in the body to allow the Frontend work based around it.
     */
    @PostMapping("/login")
    public ResponseEntity<List<String>> login(@NotNull @RequestBody LoginRequest loginRequest) {
        Keycloak keycloak = kcProvider.newKeycloakBuilderWithPasswordCredentials(loginRequest.getUsername(), loginRequest.getPassword()).build();

        AccessTokenResponse accessTokenResponse = null;
        try {
            accessTokenResponse = keycloak.tokenManager().getAccessToken();
            String accessToken = accessTokenResponse.getToken();
            DecodedJWT decodedJWT = JWT.decode(accessToken);

            Map<String, Object> resourceAccess = decodedJWT.getClaim("resource_access").asMap();
            Map<String, Object> clientRoles = (Map<String, Object>) resourceAccess.get(kcProvider.clientID);

            List<String> roles = new ArrayList<>();
            if (clientRoles != null && clientRoles.containsKey("roles")) {
                roles.addAll((List<String>) clientRoles.get("roles"));
            }

            return ResponseEntity.status(HttpStatus.OK).body(roles);
        } catch (BadRequestException ex) {
            LOG.warn("invalid account creds.", ex);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.emptyList());
        }
    }
}