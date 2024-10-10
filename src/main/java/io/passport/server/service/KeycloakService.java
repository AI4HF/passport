package io.passport.server.service;

import io.passport.server.config.KeycloakProvider;
import io.passport.server.model.Role;
import lombok.Getter;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.GroupRepresentation;
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
 * Service class for managing Keycloak operations related to user creation, group handling, and role management.
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
     * UsersResource class for managing users in Keycloak.
     */
    private final UsersResource usersResource;

    private final String realm = "your_realm_name"; // Replace with your actual realm

    @Autowired
    public KeycloakService(KeycloakProvider keycloakProvider, UsersResource usersResource) {
        this.keycloakProvider = keycloakProvider;
        this.usersResource = usersResource;
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
     * Creates a Keycloak user with the given username and password, then assigns the specified role.
     *
     * @param username the username for the new Keycloak user.
     * @param password the password for the new Keycloak user.
     * @param role the role to be assigned to the new user.
     * @return the Keycloak user ID if creation is successful.
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
        try {
            Response response = usersResource.create(user);
            if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
                response.close();
                throw new Exception("Keycloak did not return a successful response!");
            }
            String[] path = response.getLocation().getPath().split("/");
            String keycloakUserId = path[path.length - 1];
            response.close();
            this.updateRole(keycloakUserId, role);
            return Optional.of(keycloakUserId);

        } catch (Exception e) {
            log.error(e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Updates the role of an existing Keycloak user by assigning a new role.
     *
     * @param userId  the Keycloak user ID.
     * @param newRole the new role to assign to the user.
     * @return true if the role is successfully updated, false otherwise.
     */
    public boolean updateRole(String userId, Role newRole) {
        UserResource user = usersResource.get(userId);

        // Get the user's current roles
        List<RoleRepresentation> currentRoles = user.roles().realmLevel().listAll();

        // Remove current roles
        user.roles().realmLevel().remove(currentRoles);

        // Assign the new role
        Optional<RoleRepresentation> newRoleRepresentation = user.roles().realmLevel().listAvailable().stream()
                .filter(role -> role.getName().equals(newRole.name()))
                .findFirst();

        if (newRoleRepresentation.isPresent()) {
            user.roles().realmLevel().add(Collections.singletonList(newRoleRepresentation.get()));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Deletes a Keycloak user with the specified user ID.
     *
     * @param userId the Keycloak user ID.
     * @return true if the user is successfully deleted, false otherwise.
     */
    public boolean deleteUser(String userId) {
        try {
            Response response = usersResource.delete(userId);
            if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
                response.close();
                throw new Exception("Keycloak did not return a successful response!");
            }
            response.close();
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * Creates a group for the specified study and subgroups for each role within the study group.
     *
     * @param studyName the name of the study for which the groups are created.
     */
    public void createStudyGroups(String studyName) {
        RealmResource realmResource = keycloakProvider.getKeycloak().realm(realm);
        GroupRepresentation studyGroup = new GroupRepresentation();
        studyGroup.setName(studyName);


        realmResource.groups().add(studyGroup);
        try {
            GroupResource studyGroupResource = realmResource.groups().group(findGroupIdByName(studyName));


            // Create subgroups for each role
            String[] roles = {"SURVEY_MANAGER", "DATA_ENGINEER", "DATA_SCIENTIST", "QUALITY_ASSURANCE_SPECIALIST", "ML_OPS_ENGINEER", "STUDY_OWNER"};
            for (String role : roles) {
                GroupRepresentation roleGroup = new GroupRepresentation();
                roleGroup.setName(role);
                studyGroupResource.subGroup(roleGroup);
            }
        }
        catch (Exception e)
        {
            log.info(e.getMessage());
        }

    }

    /**
     * Assigns a Keycloak user to specific role groups within a study.
     *
     * @param studyName   the name of the study.
     * @param personnelId the Keycloak user ID of the personnel.
     * @param roles       the roles to assign.
     */
    public void assignPersonnelToStudyGroups(String studyName, String personnelId, List<String> roles) {
        RealmResource realmResource = keycloakProvider.getKeycloak().realm(realm);
        for (String role : roles) {
            GroupRepresentation roleGroup = findSubgroupByName(studyName, role);
            if (roleGroup != null) {
                realmResource.users().get(personnelId).joinGroup(roleGroup.getId());
            }
        }
    }

    /**
     * Helper method to find the group ID by its name.
     *
     * @param groupName the name of the group.
     * @return the group ID.
     */
    private String findGroupIdByName(String groupName) {
        return keycloakProvider.getKeycloak().realm(realm).groups().groups().stream()
                .filter(g -> g.getName().equals(groupName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Group not found"))
                .getId();
    }

    /**
     * Finds a subgroup within a parent group (study) by name.
     *
     * @param studyName the name of the parent study group.
     * @param roleName  the name of the subgroup.
     * @return the group representation of the subgroup.
     */
    private GroupRepresentation findSubgroupByName(String studyName, String roleName) {
        return keycloakProvider.getKeycloak().realm(realm).groups().group(findGroupIdByName(studyName))
                .toRepresentation()
                .getSubGroups().stream()
                .filter(g -> g.getName().equals(roleName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Removes a Keycloak user from specific role groups within a study.
     *
     * @param studyName   the name of the study.
     * @param personnelId the Keycloak user ID of the personnel.
     * @param roles       the roles to remove.
     */
    public void removePersonnelFromStudyGroups(String studyName, String personnelId, List<String> roles) {
        RealmResource realmResource = keycloakProvider.getKeycloak().realm(realm);
        for (String role : roles) {
            GroupRepresentation roleGroup = findSubgroupByName(studyName, role);
            if (roleGroup != null) {
                realmResource.users().get(personnelId).leaveGroup(roleGroup.getId());
            }
        }
    }

    /**
     * Checks if the user belongs to the 'STUDY_OWNER' group for a given study.
     *
     * @param studyId  the ID of the study.
     * @param userId   the ID of the user.
     * @return true if the user belongs to the study owner group, false otherwise.
     */
    public boolean isUserInStudyOwnerGroup(Long studyId, String userId) {
        UsersResource usersResource = keycloakProvider.getKeycloak().realm(realm).users();
        UserResource user = usersResource.get(userId);

        List<GroupRepresentation> groups = user.groups();
        String expectedGroupName = "study-" + studyId + "-STUDY_OWNER";  // Assuming groups are named with this convention

        return groups.stream()
                .anyMatch(group -> group.getName().equals(expectedGroupName));
    }

    /**
     * Checks if the user is a member of any of the role groups in a specific study.
     * @param studyId the ID of the study.
     * @param personnelId the ID of the personnel (Keycloak user).
     * @param allowedRoles the list of roles to check.
     * @return true if the user belongs to any of the allowed roles in the study, false otherwise.
     */
    public boolean isUserInStudyGroupWithRoles(Long studyId, String personnelId, List<String> allowedRoles) {
        RealmResource realmResource = keycloakProvider.getKeycloak().realm(realm);
        UsersResource usersResource = realmResource.users();
        UserResource userResource = usersResource.get(personnelId);

        // Fetch the groups the user belongs to
        List<GroupRepresentation> userGroups = userResource.groups();

        // Check for matching groups in the study with the allowed roles
        String studyGroupPrefix = "study-" + studyId + "-";
        for (GroupRepresentation group : userGroups) {
            for (String role : allowedRoles) {
                String expectedGroupName = studyGroupPrefix + role;
                if (group.getName().equals(expectedGroupName)) {
                    return true;
                }
            }
        }

        return false;  // Return false if no matching group is found
    }
}
