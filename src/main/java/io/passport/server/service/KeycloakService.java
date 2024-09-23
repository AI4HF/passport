package io.passport.server.service;

import io.passport.server.config.KeycloakProvider;
import io.passport.server.model.Role;
import lombok.Getter;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Configuration;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
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
    public AccessTokenResponse getAccessToken(String username, String password) {
        Keycloak keycloak = this.keycloakProvider.newKeycloakBuilderWithPasswordCredentials(username, password);
        AccessTokenResponse tokenResponse = keycloak.tokenManager().grantToken();
        keycloak.close();
        return tokenResponse;
    }

    /**
     * Create a Keycloak user by username and password
     * @param username user Keycloak recorded username
     * @param password user Keycloak recorded password
     * @param role user realm role
     * @return
     */
    public Optional<String> createUserAndReturnId(String username, String password, Role role) {
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
            String keycloakUserId = path[path.length - 1];
            response.close();
            this.updateRole(keycloakUserId, role);
            return Optional.of(keycloakUserId);

        }catch (Exception e) {
            log.error(e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Update a Keycloak user role
     * @param userId ID of Keycloak user
     * @param newRole role that will assign
     * @return
     */
    public boolean updateRole(String userId, Role newRole) {
        UserResource user = usersResource.get(userId);

        // Get the user's current roles
        List<RoleRepresentation> currentRoles = user.roles().realmLevel().listAll();

        // Remove current roles
        user.roles().realmLevel().remove(currentRoles);

        // Convert the newRole enum to RoleRepresentation
        Optional<RoleRepresentation> newRoleRepresentation = user.roles().realmLevel().listAvailable().stream()
                .filter(role -> role.getName().equals(newRole.name()))
                .findFirst();

        if(newRoleRepresentation.isPresent()) {
            // Add new role
            user.roles().realmLevel().add(Collections.singletonList(newRoleRepresentation.get()));
            return true;
        }else{
            return false;
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