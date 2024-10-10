package io.passport.server.service;

import io.passport.server.model.Role;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Service class for role checking and authorization handling
 */
@Service
public class RoleCheckerService {

    @Autowired
    private KeycloakService keycloakService;

    /**
     * Check if the user has any of the roles from the rolesToCheck list based on the access token.
     * @param principal KeycloakPrincipal object containing the access token
     * @param rolesToCheck The list of roles to check
     * @return true if the user has at least one role from the rolesToCheck list, false otherwise
     */
    public boolean hasAnyRole(KeycloakPrincipal<?> principal, List<Role> rolesToCheck) {
        if (principal == null || rolesToCheck == null || rolesToCheck.isEmpty()) {
            return false;
        }

        KeycloakSecurityContext keycloakSecurityContext = principal.getKeycloakSecurityContext();
        AccessToken token = keycloakSecurityContext.getToken();
        Set<String> userRoles = token.getRealmAccess().getRoles();

        // Check if any of the roles in rolesToCheck is present in the user's roles
        for (Role role : rolesToCheck) {
            if (userRoles.contains(role.toString())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if a user is authorized to perform actions for a specific study by verifying their membership
     * in the relevant study groups.
     * @param studyId ID of the study
     * @param principal KeycloakPrincipal object containing the access token
     * @param allowedRoles List of roles allowed to access the study
     * @return true if the user is a member of one of the allowed roles for the given study, false otherwise
     */
    public boolean isUserAuthorizedForStudy(Long studyId, KeycloakPrincipal<?> principal, List<Role> allowedRoles) {
        String personnelId = getPersonnelId(principal);
        List<String> allowedRoleNames = allowedRoles.stream().map(Role::toString).toList();

        // Check if the user is a member of any of the allowed role groups within the study
        return keycloakService.isUserInStudyGroupWithRoles(studyId, personnelId, allowedRoleNames);
    }

    /**
     * Extract personnel ID from the access token
     * @param principal KeycloakPrincipal object containing the access token
     * @return Personnel ID from the token
     */
    public String getPersonnelId(KeycloakPrincipal<?> principal) {
        KeycloakSecurityContext keycloakSecurityContext = principal.getKeycloakSecurityContext();
        AccessToken token = keycloakSecurityContext.getToken();
        return token.getOtherClaims().get("user_id").toString();
    }
}
