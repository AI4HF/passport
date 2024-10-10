package io.passport.server.config;

import lombok.Getter;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Class which provides Keycloak instance and necessary resources for interacting with Keycloak server.
 */
@Configuration
@Getter
public class KeycloakProvider {

    /**
     * Keycloak variables that are necessary to access the desired Keycloak Realm and Client.
     * Values are provided in resources/application.properties
     */
    @Value("${keycloak.auth-server-url}")
    private String serverURL;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientID;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    /**
     * Provides a Keycloak instance configured with client credentials.
     * @return a configured Keycloak instance.
     */
    @Bean
    public Keycloak getKeycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(serverURL)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientID)
                .clientSecret(clientSecret)
                .build();
    }

    /**
     * Provides the RealmResource for the configured realm in Keycloak.
     * @param keycloak the Keycloak instance.
     * @return the RealmResource for the realm.
     */
    @Bean
    public RealmResource realmResource(Keycloak keycloak) {
        return keycloak.realm(realm);
    }

    /**
     * Provides the UsersResource for managing users in the configured realm.
     * @param realmResource the RealmResource instance.
     * @return the UsersResource for user management.
     */
    @Bean
    public UsersResource usersResource(RealmResource realmResource) {
        return realmResource.users();
    }

    /**
     * Provides a Keycloak instance configured with password credentials for user-specific actions.
     * @param username the username for Keycloak login.
     * @param password the password for Keycloak login.
     * @return a configured Keycloak instance with password credentials.
     */
    public Keycloak newKeycloakBuilderWithPasswordCredentials(String username, String password) {
        return KeycloakBuilder.builder()
                .realm(realm)
                .serverUrl(serverURL)
                .clientId(clientID)
                .clientSecret(clientSecret)
                .username(username)
                .password(password)
                .grantType(OAuth2Constants.PASSWORD)
                .build();
    }
}
