package io.passport.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * StudyOrganization DTO model for managing StudyOrganization objects.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StudyOrganizationDTO {

    private String organizationId;

    private String studyId;

    private String personnelId;

    private Set<Role> roles;

    private String populationId;

    public StudyOrganizationDTO(StudyOrganization studyOrganization) {
        this.organizationId = studyOrganization.getId().getOrganizationId();
        this.studyId = studyOrganization.getId().getStudyId();
        this.populationId = studyOrganization.getPopulationId();
        this.personnelId = studyOrganization.getResponsiblePersonnelId();

        String[] roleString = studyOrganization.getRole().split(",");
        Set<Role> roleSet = new HashSet<>();
        for (String role : roleString) {
            roleSet.add(Role.valueOf(role));
        }
        this.roles = roleSet;
    }
}
