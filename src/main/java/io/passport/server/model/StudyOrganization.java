package io.passport.server.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

/**
 * StudyOrganization model used for the StudyOrganization management.
 */
@Entity
@Table(name = "study_organization")
@Getter
@Setter
@NoArgsConstructor
public class StudyOrganization {

    @EmbeddedId
    private StudyOrganizationId id;

    @Column(name = "role")
    private String role;

    @Column(name = "responsible_personnel_id")
    private String responsiblePersonnelId;

    @Column(name = "population_Id")
    private Long populationId;

    public StudyOrganization(StudyOrganizationDTO studyOrganizationDTO) {
        //Construct ID
        StudyOrganizationId studyOrganizationId = new StudyOrganizationId();
        studyOrganizationId.setOrganizationId(studyOrganizationDTO.getOrganizationId());
        studyOrganizationId.setStudyId(studyOrganizationDTO.getStudyId());
        this.id = studyOrganizationId;

        // Construct role string
        StringBuilder stringBuilder = new StringBuilder();
        studyOrganizationDTO.getRoles().forEach(role -> {
            stringBuilder.append(role).append(",");
        });
        if(stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        this.role = stringBuilder.toString();

        this.responsiblePersonnelId = studyOrganizationDTO.getPersonnelId();

        this.populationId = studyOrganizationDTO.getPopulationId();
    }
}
