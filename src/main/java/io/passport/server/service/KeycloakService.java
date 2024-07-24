package io.passport.server.service;

import io.passport.server.config.KeycloakProvider;
import lombok.Getter;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Configuration;

/**
 * Service class for Keycloak.
 */
@Configuration
@Getter
public class KeycloakService {

    /**
     * Provider class for the Keycloak.
     */
    private final KeycloakProvider keycloakProvider;


    @Autowired
    public KeycloakService(KeycloakProvider keycloakProvider) {
        this.keycloakProvider = keycloakProvider;
    }

    /**
     * Login with user credentials and acquire an access token.
     * @param username user Keycloak recorded username
     * @param password user Keycloak recorded password
     * @return
     */
    public AccessTokenResponse getAccessToken(String username, String password) {
        Keycloak keycloak = this.keycloakProvider.newKeycloakBuilderWithPasswordCredentials(username, password);
        AccessTokenResponse tokenResponse = keycloak.tokenManager().grantToken();
        keycloak.close();
        return tokenResponse;
    }

}