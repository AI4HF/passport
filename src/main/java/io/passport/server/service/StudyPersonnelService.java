package io.passport.server.service;

import io.passport.server.model.*;
import io.passport.server.repository.StudyPersonnelRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for StudyPersonnel management.
 */
@Service
public class StudyPersonnelService {

    /**
     * StudyPersonnel repo access for database management.
     */
    private final StudyPersonnelRepository studyPersonnelRepository;

    /**
     * Personnel service for personnel details
     */
    private final PersonnelService personnelService;

    /**
     * Keycloak service for role assignments on Keycloak
     */
    private final KeycloakService keycloakService;
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public StudyPersonnelService(StudyPersonnelRepository studyPersonnelRepository,
                                 PersonnelService personnelService,
                                 KeycloakService keycloakService,
                                 RoleCheckerService roleCheckerService) {
        this.studyPersonnelRepository = studyPersonnelRepository;
        this.personnelService = personnelService;
        this.keycloakService = keycloakService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Determines which entities are to be cascaded based on the request from the previous element in the chain
     * Continues the chain by directing to the next entries through the other validation method
     *
     * @param studyId Id of the Study
     * @param sourceResourceType Resource type of the parent element in the Cascade chain
     * @param sourceResourceId Resource id of the parent element in the Cascade chain
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateCascade(String studyId, String sourceResourceType, String sourceResourceId, Jwt principal) {
        List<StudyPersonnel> affectedEntries;

        switch (sourceResourceType) {
            case "Study":
                affectedEntries = studyPersonnelRepository.findByIdStudyId(sourceResourceId);
                break;
            case "Personnel":
                affectedEntries = studyPersonnelRepository.findStudyPersonnelById_PersonnelId(sourceResourceId);
                break;
            default:
                return new ValidationResult(true, "");
        }

        if (affectedEntries.isEmpty()) {
            return new ValidationResult(true, "");
        }

        boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                studyId,
                principal,
                List.of(Role.STUDY_OWNER)
        );

        if (!hasPermission) {
            return new ValidationResult(false, "StudyPersonnel");
        }

        return new ValidationResult(true, "StudyPersonnel");
    }
    /**
     * Fetch personnel-role mappings for a given study and organization.
     *
     * @param studyId        The ID of the study.
     * @param organizationId The ID of the organization.
     * @return A Map of personnel IDs to their roles.
     */
    public Map<String, List<String>> getPersonnelRolesByStudyAndOrganization(String studyId, String organizationId) {
        List<StudyPersonnel> studyPersonnelList =
                studyPersonnelRepository.findStudyPersonnelByStudyIdAndOrganizationId(studyId, organizationId);

        // Transform StudyPersonnel entries into a Map of personnelId to roles
        return studyPersonnelList.stream()
                .collect(Collectors.toMap(
                        sp -> sp.getId().getPersonnelId(), // Map key: personnelId
                        StudyPersonnel::getRolesAsList   // Map value: list of roles
                ));
    }

    /**
     * Get all personnel related to the study and organization.
     * @param studyId ID of the study
     * @param organizationId ID of the organization
     * @return
     */
    public List<Personnel> findPersonnelByStudyIdAndOrganizationId(String studyId, String organizationId){
        return this.studyPersonnelRepository.findPersonnelByStudyIdAndOrganizationId(studyId, organizationId);
    }

    /**
     * Get all studies related to the personnel.
     * @param personnelId ID of the personnel
     * @return
     */
    public List<Study> findStudiesByPersonnelId(String personnelId){
        return this.studyPersonnelRepository.findStudiesByPersonnelId(personnelId);
    }

    /**
     * Delete StudyPersonnel entries by studyId and PersonnelId.
     * When the roles of the given personnel are deleted,
     * their role memberships in the study with the given ID must also be deleted from Keycloak.
     * @param studyId ID of the study
     * @param personnelIdList ID list for personnel
     */
    @Transactional
    public void clearStudyPersonnelEntriesByStudyIdAndPersonnelId(String studyId, List<String> personnelIdList) {
        String studyName = "study-" + studyId;

        List<StudyPersonnel> studyPersonnelList = studyPersonnelRepository.findByStudyIdAndPersonnelIdList(studyId, personnelIdList);

        studyPersonnelList.forEach(studyPersonnel -> {
            List<String> roles = studyPersonnel.getRolesAsList();
            String personnelId = studyPersonnel.getId().getPersonnelId();
            keycloakService.removePersonnelFromStudyGroups(studyName, personnelId, roles);
        });
        studyPersonnelRepository.deleteByStudyIdAndPersonnelId(studyId, personnelIdList);
    }


    /**
     * Create StudyPersonnel entries with role assignments and Keycloak memberships.
     * @param studyId ID of the study
     * @param organizationId ID of the organization
     * @param personnelRoleMap Map of Personnel and their corresponding roles
     */
    @Transactional
    public void createStudyPersonnelEntries(String studyId, String organizationId, Map<String, List<String>> personnelRoleMap) {
        // Fetch existing personnel for the organization and clear their StudyPersonnel entries
        List<String> personnelIdList = this.personnelService.findPersonnelByOrganizationId(organizationId).stream()
                .map(Personnel::getPersonId).collect(Collectors.toList());

        // Clear existing personnel entries and remove their roles from Keycloak

        // Process each Personnel and their roles
        List<StudyPersonnel> studyPersonnelEntries = personnelRoleMap.entrySet().stream().map(entry -> {
            String personnel = entry.getKey();
            List<String> roles = entry.getValue();

            // Create a StudyPersonnel entry for this personnel with the assigned roles
            StudyPersonnel studyPersonnel = new StudyPersonnel();
            StudyPersonnelId studyPersonnelId = new StudyPersonnelId();
            studyPersonnelId.setStudyId(studyId);
            studyPersonnelId.setPersonnelId(personnel);
            studyPersonnel.setId(studyPersonnelId);
            studyPersonnel.setRolesFromList(roles);

            keycloakService.assignPersonnelToStudyGroups(studyId, personnel, roles);

            return studyPersonnel;
        }).collect(Collectors.toList());

        // Save all the new StudyPersonnel entries
        studyPersonnelRepository.saveAll(studyPersonnelEntries);
    }

    /**
     * Find all StudyPersonnel entries related to the given personId.
     * @param personId ID of the personnel.
     * @return List of StudyPersonnel entries.
     */
    public List<StudyPersonnel> findStudyPersonnelByPersonId(String personId) {
        return studyPersonnelRepository.findStudyPersonnelById_PersonnelId(personId);
    }

}
