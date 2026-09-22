package io.passport.server.service;

import io.passport.server.model.Dataset;
import io.passport.server.model.Personnel;
import io.passport.server.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Organization-level authorization, which study group membership alone cannot express.
 *
 * A Data Steward is scoped to their own organization within a study: they complete the publication
 * metadata for the datasets their organization extracted, and must not touch another organization's.
 * Keycloak study groups carry the role but not the organization, so that half of the check lives
 * here rather than in {@link RoleCheckerService}.
 */
@Service
public class OrganizationScopeService {

    private final RoleCheckerService roleCheckerService;

    @Autowired @Lazy private PersonnelService personnelService;
    @Autowired @Lazy private DatasetService datasetService;

    @Autowired
    public OrganizationScopeService(RoleCheckerService roleCheckerService) {
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Checks that the caller may write the publication metadata of a dataset: authorized for the
     * study in an allowed role, and - when acting as a Data Steward - belonging to the organization
     * that owns the dataset.
     *
     * @param studyId     Id of the Study, the authorization scope
     * @param datasetId   Id of the Dataset whose metadata is being written
     * @param principal   Access token of the caller
     * @param allowedRoles Roles allowed to write at all
     * @return true when the caller may write
     */
    public boolean isCallerAuthorizedForDatasetMetadata(String studyId, String datasetId, Jwt principal,
                                                        List<Role> allowedRoles) {
        if (!roleCheckerService.isUserAuthorizedForStudy(studyId, principal, allowedRoles)) {
            return false;
        }

        // Only the steward is organization-scoped; a data engineer works across the study.
        boolean isSteward = roleCheckerService.isUserAuthorizedForStudy(studyId, principal, List.of(Role.DATA_STEWARD));
        boolean hasBroaderRole = roleCheckerService.isUserAuthorizedForStudy(
                studyId, principal, List.of(Role.DATA_ENGINEER, Role.STUDY_OWNER));
        if (!isSteward || hasBroaderRole) {
            return true;
        }

        return belongsToSameOrganization(principal.getSubject(), datasetId);
    }

    /**
     * Whether the caller's organization is the one that owns the dataset.
     *
     * @param personnelId Keycloak id of the caller, which is also the personnel id
     * @param datasetId   Id of the Dataset
     * @return true when both sides resolve and match
     */
    public boolean belongsToSameOrganization(String personnelId, String datasetId) {
        Optional<Personnel> personnel = personnelService.findPersonnelById(personnelId);
        Optional<Dataset> dataset = datasetService.findDatasetByDatasetId(datasetId);

        if (personnel.isEmpty() || dataset.isEmpty()) {
            return false;
        }

        String personnelOrganizationId = personnel.get().getOrganizationId();
        String datasetOrganizationId = dataset.get().getOrganizationId();

        return personnelOrganizationId != null && personnelOrganizationId.equals(datasetOrganizationId);
    }
}
