package io.passport.server.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.passport.server.config.KeycloakProvider;
import io.passport.server.model.Role;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing Keycloak operations related to user creation, group handling, and role management.
 */
@Configuration
@Getter
public class KeycloakService {

    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final Logger log = LoggerFactory.getLogger(KeycloakService.class);

    private final KeycloakProvider keycloakProvider;
    private final UsersResource usersResource;
    private final String realm;
    private final Keycloak keycloak;

    @Autowired
    public KeycloakService(KeycloakProvider keycloakProvider) {
        this.keycloakProvider = keycloakProvider;
        this.keycloak = keycloakProvider.getKeycloak();
        this.realm = keycloakProvider.getRealm();
        this.usersResource = keycloak.realm(realm).users();
    }

    /**
     * Login with user credentials and acquire an access token.
     * @param username user Keycloak recorded username
     * @param password user Keycloak recorded password
     * @return AccessTokenResponse
     */
    public AccessTokenResponse getAccessToken(String username, String password) {
        Keycloak keycloakWithCredentials = this.keycloakProvider.newKeycloakBuilderWithPasswordCredentials(username, password);
        AccessTokenResponse tokenResponse = keycloakWithCredentials.tokenManager().grantToken();
        keycloakWithCredentials.close(); // Close the instance used for token
        return tokenResponse;
    }

    /**
     * Creates a Keycloak user with the given username and password, then assigns the specified role.
     * @param username the username for the new Keycloak user.
     * @param password the password for the new Keycloak user.
     * @param role the role to be assigned to the new user.
     * @return Optional<String> Keycloak user ID if creation is successful.
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
     * @param userId  the Keycloak user ID.
     * @param newRole the new role to assign to the user.
     * @return true if the role is successfully updated, false otherwise.
     */
    public boolean updateRole(String userId, Role newRole) {
        if (newRole == null) {
            return true;
        }
        UserResource user = usersResource.get(userId);

        // Get the user's current roles
        List<RoleRepresentation> currentRoles = user.roles().realmLevel().listAll();

        // Remove current roles
        user.roles().realmLevel().remove(currentRoles);
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
     * Initializes a study group for a newly created study, with the creator as its owner.
     * @param studyId Id of the study that has been created.
     * @param ownerId Creator id.
     */
    public void createStudyGroups(Long studyId, String ownerId) {
        // Create the main group
        String groupName = "study-" + studyId;
        GroupRepresentation group = new GroupRepresentation();
        group.setName(groupName);

        Response response = keycloak.realm(realm).groups().add(group);

        if (response.getStatus() != 201) {
            throw new RuntimeException("Failed to create group: " + groupName);
        }
        String groupId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

        List<String> subgroupNames = Arrays.asList(
                "STUDY_OWNER",
                "DATA_ENGINEER",
                "DATA_SCIENTIST",
                "SURVEY_MANAGER",
                "QUALITY_ASSURANCE_SPECIALIST",
                "ML_ENGINEER"
        );

        for (String subgroupName : subgroupNames) {
            GroupRepresentation subgroup = new GroupRepresentation();
            subgroup.setName(subgroupName);
            keycloak.realm(realm).groups().group(groupId).subGroup(subgroup);
        }
        assignPersonnelToStudyGroups(studyId, ownerId, List.of("STUDY_OWNER"));
    }

    /**
     * Assign personnel to subgroups for a specific study.
     * This method will overwrite the user's existing group memberships.
     * If the roles list is empty, the user will be removed from all subgroups for that study.
     *
     * @param studyId the ID of the study
     * @param personnelId the ID of the personnel (user)
     * @param roles the list of roles to assign
     */
    public void assignPersonnelToStudyGroups(Long studyId, String personnelId, List<String> roles) {
        // Retrieve all subgroups of the study
        GroupRepresentation studyGroup = getGroupByName("study-" + studyId);
        List<GroupRepresentation> subgroups = keycloak.realm(realm).groups().group(studyGroup.getId()).getSubGroups(0, 100, true);
        List<GroupRepresentation> subgroups2 = subgroups.stream().filter(subgroup -> !subgroup.getName().equals("STUDY_OWNER")).collect(Collectors.toList());
        for (GroupRepresentation subgroup : subgroups2) {
            keycloak.realm(realm).users().get(personnelId).leaveGroup(subgroup.getId());
        }

        for (String role : roles) {
            GroupRepresentation subgroup = subgroups.stream()
                    .filter(g -> g.getName().equals(role))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Subgroup for role " + role + " not found."));
            keycloak.realm(realm).users().get(personnelId).joinGroup(subgroup.getId());
        }
    }
    /**
     * Get the group by its name.
     * @param groupName the name of the group
     * @return the GroupRepresentation of the group
     */
    private GroupRepresentation getGroupByName(String groupName) {
        GroupsResource groups = keycloak.realm(realm).groups();

        return groups.groups().stream()
                .filter(group -> group.getName().equalsIgnoreCase(groupName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Group not found: " + groupName));
    }

    /**
     * Get group ID by group name.
     * @param groupName the name of the group
     * @return the ID of the group or null if not found
     */
    public String findGroupIdByName(String groupName) {
        List<GroupRepresentation> groups = keycloak.realm(realm).groups().groups();

        for (GroupRepresentation group : groups) {
            if (group.getName().equalsIgnoreCase(groupName)) {
                return group.getId();
            }
        }
        return null;
    }

    /**
     * Finds a subgroup within a parent group (study) by name.
     * @param studyName the name of the parent study group.
     * @param roleName  the name of the subgroup.
     * @return the group representation of the subgroup.
     */
    private GroupRepresentation findSubgroupByName(String studyName, String roleName) {
        return keycloak.realm(realm).groups().group(findGroupIdByName(studyName))
                .toRepresentation()
                .getSubGroups().stream()
                .filter(g -> g.getName().equals(roleName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Removes a Keycloak user from specific role groups within a study.
     * @param studyName   the name of the study.
     * @param personnelId the Keycloak user ID of the personnel.
     * @param roles       the roles to remove.
     */
    public void removePersonnelFromStudyGroups(String studyName, String personnelId, List<String> roles) {
        RealmResource realmResource = keycloak.realm(realm);
        for (String role : roles) {
            GroupRepresentation roleGroup = findSubgroupByName(studyName, role);
            if (roleGroup != null) {
                realmResource.users().get(personnelId).leaveGroup(roleGroup.getId());
            }
        }
    }

    /**
     * Checks if the user belongs to the 'STUDY_OWNER' group for a given study.
     * @param studyId  the ID of the study.
     * @param userId   the ID of the user.
     * @return true if the user belongs to the study owner group, false otherwise.
     */
    public boolean isUserInStudyOwnerGroup(Long studyId, String userId) {
        List<GroupRepresentation> groups = usersResource.get(userId).groups();
        String expectedGroupName = "study-" + studyId + "-STUDY_OWNER";  // Assuming groups are named with this convention

        return groups.stream().anyMatch(group -> group.getName().equals(expectedGroupName));
    }

    /**
     * Check if a personnel belongs to at least one subgroup for a given study
     * by searching for their ID in the members of each subgroup.
     *
     * @param studyId the ID of the study
     * @param personnelId the ID of the personnel (user)
     * @param roles the list of roles to check
     * @return true if the user is a member of at least one of the subgroups, false otherwise
     */
    public boolean isUserInStudyGroupWithRoles(Long studyId, String personnelId, List<String> roles) {
        // Retrieve the main study group
        GroupRepresentation studyGroup = getGroupByName("study-" + studyId);
        List<GroupRepresentation> subgroups = keycloak.realm(realm).groups().group(studyGroup.getId()).getSubGroups(0, 100, true);

        // Iterate through the desired roles (subgroups)
        for (String role : roles) {
            // Find the subgroup for the role
            GroupRepresentation subgroup = subgroups.stream()
                    .filter(g -> g.getName().equals(role))
                    .findFirst()
                    .orElse(null);

            if (subgroup != null) {
                // Retrieve the members of the subgroup
                List<UserRepresentation> members = keycloak.realm(realm).groups().group(subgroup.getId()).members();

                // Check if the personnelId exists in the list of members
                boolean isMember = members.stream().anyMatch(member -> member.getId().equals(personnelId));

                if (isMember) {
                    return true; // Return true if the user is found in at least one subgroup
                }
            }
        }

        return false; // Return false if the user is not found in any of the desired subgroups
    }

    /**
     * Retrieves all realm-level roles assigned to a user.
     *
     * @param userId the ID of the user in Keycloak.
     * @return a set of roles assigned to the user.
     */
    public Set<String> getUserRoles(String userId) {
        UserResource userResource = usersResource.get(userId);
        List<RoleRepresentation> userRoles = userResource.roles().realmLevel().listEffective();

        return userRoles.stream()
                .map(RoleRepresentation::getName)
                .collect(Collectors.toSet());
    }
}
