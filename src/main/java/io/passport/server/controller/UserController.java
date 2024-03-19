package io.passport.server.controller;

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
     * Main login method which handles the Keycloak Authentication process, access token and resource_access retrieval.
     * Extracts the user's roles on the client and returns them in the body to allow the Frontend work based around it.
     */
    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponseDto> login(@NotNull @RequestBody LoginRequest loginRequest) {
        try {
            Keycloak keycloak = kcProvider.getKeycloakWithResources(loginRequest.getUsername(), loginRequest.getPassword());
            AccessTokenResponse accessTokenResponse = keycloak.tokenManager().getAccessToken();
            String accessToken = accessTokenResponse.getToken();
            AccessTokenResponseDto responseDto = new AccessTokenResponseDto(accessToken);
            return ResponseEntity.status(HttpStatus.OK).body(responseDto);
        } catch (BadRequestException ex) {
            LOG.warn("invalid account creds.", ex);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null); // Returning null or an empty DTO based on your preference
        }
    }
    public class AccessTokenResponseDto {
        private String accessToken;

        public AccessTokenResponseDto(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }
}