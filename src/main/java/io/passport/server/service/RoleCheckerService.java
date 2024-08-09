package io.passport.server.service;

import io.passport.server.model.Role;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Service class for role checking
 */
@Service
public class RoleCheckerService {

    /**
     * Check access token that contains at least one of the role from rolesToCheck.
     * @param principal KeycloakPrincipal object that contains access token
     * @param rolesToCheck The list of roles that will be checked
     */
    public boolean hasAnyRole(KeycloakPrincipal<?> principal, List<Role> rolesToCheck) {
        if (principal == null || rolesToCheck == null || rolesToCheck.isEmpty()) {
            return false;
        }

        KeycloakSecurityContext keycloakSecurityContext = principal.getKeycloakSecurityContext();
        AccessToken token = keycloakSecurityContext.getToken();
        Set<String> userRoles = token.getRealmAccess().getRoles();

        // Check if at least one of the rolesToCheck is present in the user's roles
        for (Role role : rolesToCheck) {
            if (userRoles.contains(role.toString())) {
                return true;
            }
        }

        return false;
    }
}
