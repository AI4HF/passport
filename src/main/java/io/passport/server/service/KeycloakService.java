package io.passport.server.service;

import io.passport.server.config.KeycloakProvider;
import io.passport.server.model.LoginResponse;
import lombok.Getter;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Configuration;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Optional;

/**
 * Service class for Keycloak.
 */
@Configuration
@Getter
public class KeycloakService {

    private static final Logger log = LoggerFactory.getLogger(KeycloakService.class);

    /**
     * Provider class for the Keycloak.
     */
    private final KeycloakProvider keycloakProvider;

    /**
     * UsersResource class for managing users in the Keycloak.
     */
    private final UsersResource usersResource;

    @Autowired
    public KeycloakService(KeycloakProvider keycloakProvider, UsersResource userResource) {
        this.keycloakProvider = keycloakProvider;
        this.usersResource = userResource;
    }

    /**
     * Login with user credentials and acquire an access token.
     * @param username user Keycloak recorded username
     * @param password user Keycloak recorded password
     * @return
     */
    public LoginResponse getAccessToken(String username, String password) {
        Keycloak keycloak = this.keycloakProvider.newKeycloakBuilderWithPasswordCredentials(username, password);
        String userId = this.usersResource.search(username).get(0).getId();
        AccessTokenResponse tokenResponse = keycloak.tokenManager().grantToken();
        keycloak.close();
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAuthResponse(tokenResponse);
        loginResponse.setUserId(userId);

        return loginResponse;
    }

    /**
     * Create a Keycloak user by username and password
     * @param username user Keycloak recorded username
     * @param password user Keycloak recorded password
     * @return
     */
    public Optional<String> createUserAndReturnId(String username, String password) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEnabled(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);

        user.setCredentials(Collections.singletonList(credential));
        try{
            Response response = usersResource.create(user);
            if(!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
                response.close();
                throw new Exception("Keycloak does not return successful response!");
            }
            String[] path = response.getLocation().getPath().split("/");
            response.close();
            return Optional.of(path[path.length - 1]);

        }catch (Exception e) {
            log.error(e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Delete a Keycloak user by userId
     * @param userId ID of Keycloak user
     * @return
     */
    public boolean deleteUser(String userId) {
        try{
            Response response = usersResource.delete(userId);
            if(!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
                response.close();
                throw new Exception("Keycloak does not return successful response!");
            }
            response.close();
            return true;
        }catch(Exception e){
            log.error(e.getMessage());
            return false;
        }
    }
}