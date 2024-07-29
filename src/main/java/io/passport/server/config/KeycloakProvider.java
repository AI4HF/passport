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
 * Class which allows the Keycloak methods to be called on Spring.
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

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(serverURL)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientID)
                .clientSecret(clientSecret)
                .build();
    }

    @Bean
    public RealmResource realmResource(Keycloak keycloak) {
        return keycloak.realm(realm);
    }

    @Bean
    public UsersResource usersResource(RealmResource realmResource) {
        return realmResource.users();
    }

    /**
     * Called to create an instance of the Keycloak in Spring
     * @param username user Keycloak recorded username
     * @param password user Keycloak recorded password
     * @return
     */
    public Keycloak newKeycloakBuilderWithPasswordCredentials(String username, String password) {
        return (KeycloakBuilder.builder()
                .realm(realm)
                .serverUrl(serverURL)
                .clientId(clientID)
                .clientSecret(clientSecret)
                .username(username)
                .password(password)).build();
    }

}