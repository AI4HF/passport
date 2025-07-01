package io.passport.server.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.passport.server.config.KeycloakProvider;
import io.passport.server.model.Role;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
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

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing Keycloak operations related to user creation, group handling, and role management.
 */
@Configuration
@Getter
public class KeycloakService {

    private static final String OFFLINE_ROLE_NAME = "offline_access";

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
     * Issue a long-term Refresh Token for the user with given credentials.
     * @param username Username of the user for the Refresh Token
     * @param password Password of the user for the Refresh Token
     * @return The Refresh Token
     */
    public String createOfflineSecret(String username, String password) {

        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token",
                keycloakProvider.getServerURL(), keycloakProvider.getRealm());

        List<NameValuePair> body = List.of(
                new BasicNameValuePair("grant_type",    "password"),
                new BasicNameValuePair("client_id",     keycloakProvider.getClientID()),
                new BasicNameValuePair("client_secret", keycloakProvider.getClientSecret()),
                new BasicNameValuePair("username",      username),
                new BasicNameValuePair("password",      password),
                new BasicNameValuePair("scope",         "offline_access")
        );

        try (CloseableHttpClient http = HttpClients.createDefault()) {

            HttpPost post = new HttpPost(tokenUrl);
            post.setEntity(new UrlEncodedFormEntity(body));

            try (var response = http.execute(post)) {
                String json = EntityUtils.toString(response.getEntity());
                AccessTokenResponse atr = objectMapper.readValue(json, AccessTokenResponse.class);

                return atr.getRefreshToken();
            }

        } catch (IOException e) {
            throw new RuntimeException("Unable to obtain offline token", e);
        }
    }


    /**
     * Redeem the Refresh Token for an actual Access Token.
     * @param offlineRefreshToken The secret given to the user.
     * @return Access Token response with an Access Token
     */
    public AccessTokenResponse refreshWithSecret(String offlineRefreshToken) {

        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token",
                keycloakProvider.getServerURL(), realm);

        List<NameValuePair> body = List.of(
                new BasicNameValuePair(OAuth2Constants.GRANT_TYPE,  OAuth2Constants.REFRESH_TOKEN),
                new BasicNameValuePair(OAuth2Constants.REFRESH_TOKEN, offlineRefreshToken),
                new BasicNameValuePair(OAuth2Constants.CLIENT_ID,   keycloakProvider.getClientID()),
                new BasicNameValuePair("client_secret",             keycloakProvider.getClientSecret())
        );

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(tokenUrl);
            post.setEntity(new UrlEncodedFormEntity(body));

            String json = EntityUtils.toString(client.execute(post).getEntity());
            AccessTokenResponse atr = objectMapper.readValue(json, AccessTokenResponse.class);

            if (atr.getToken() == null || atr.getToken().isBlank()) {
                throw new IllegalStateException("Keycloak did not return an access_token - refresh token may be expired or revoked.");
            }
            return atr;

        } catch (IOException e) {
            throw new RuntimeException("Could not refresh token", e);
        }
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
        UserResource user = usersResource.get(userId);

        RoleRepresentation offlineRole = keycloak.realm(realm).roles().get(OFFLINE_ROLE_NAME).toRepresentation();

        List<RoleRepresentation> current = user.roles().realmLevel().listAll();
        List<RoleRepresentation> toRemove = current.stream()
                .filter(r -> !r.getName().equals(OFFLINE_ROLE_NAME))
                .collect(Collectors.toList());
        if (!toRemove.isEmpty()) {
            user.roles().realmLevel().remove(toRemove);
        }

        if (current.stream().noneMatch(r -> r.getName().equals(OFFLINE_ROLE_NAME))) {
            user.roles().realmLevel().add(Collections.singletonList(offlineRole));
        }

        if (newRole != null) {
            RoleRepresentation domainRole =
                    keycloak.realm(realm).roles().get(newRole.name()).toRepresentation();
            user.roles().realmLevel().add(Collections.singletonList(domainRole));
        }
        return true;
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
    public void createStudyGroups(String studyId, String ownerId) {
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
    public void assignPersonnelToStudyGroups(String studyId, String personnelId, List<String> roles) {
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
     * Checks if the user beStrings to the 'STUDY_OWNER' group for a given study.
     * @param studyId  the ID of the study.
     * @param userId   the ID of the user.
     * @return true if the user beStrings to the study owner group, false otherwise.
     */
    public boolean isUserInStudyOwnerGroup(String studyId, String userId) {
        List<GroupRepresentation> groups = usersResource.get(userId).groups();
        String expectedGroupName = "study-" + studyId + "-STUDY_OWNER";  // Assuming groups are named with this convention

        return groups.stream().anyMatch(group -> group.getName().equals(expectedGroupName));
    }

    /**
     * Check if a personnel beStrings to at least one subgroup for a given study
     * by searching for their ID in the members of each subgroup.
     *
     * @param studyId the ID of the study
     * @param personnelId the ID of the personnel (user)
     * @param roles the list of roles to check
     * @return true if the user is a member of at least one of the subgroups, false otherwise
     */
    public boolean isUserInStudyGroupWithRoles(String studyId, String personnelId, List<String> roles) {
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
