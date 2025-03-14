package io.passport.server.service;

import io.passport.server.model.Role;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for role checking and authorization handling
 */
@Service
public class RoleCheckerService {

    @Autowired
    private KeycloakService keycloakService;

    /**
     * Check if the user has any of the roles from the rolesToCheck list based on the access token.
     * @param principal Jwt object containing the access token
     * @param rolesToCheck The list of roles to check
     * @return true if the user has at least one role from the rolesToCheck list, false otherwise
     */
    public boolean hasAnyRole(Jwt principal, List<Role> rolesToCheck) {
        if (principal == null || rolesToCheck == null || rolesToCheck.isEmpty()) {
            return false;
        }

        Set<String> userRoles = keycloakService.getUserRoles(principal.getSubject());

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
    public boolean isUserAuthorizedForStudy(String studyId, Jwt principal, List<Role> allowedRoles) {
        String personnelId = principal.getSubject();
        List<String> allowedRoleNames = allowedRoles.stream().map(Role::toString).collect(Collectors.toList());

        // Check if the user is a member of any of the allowed role groups within the study
        return keycloakService.isUserInStudyGroupWithRoles(studyId, personnelId, allowedRoleNames);
    }
}
