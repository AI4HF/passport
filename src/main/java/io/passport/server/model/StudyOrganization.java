package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.passport.server.model.composite_keys.StudyOrganizationKey;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "study_organization")
@Getter
@Setter
public class StudyOrganization {

    @EmbeddedId
    private StudyOrganizationKey id;

    @ManyToOne
    @MapsId("studyId")
    @JoinColumn(name = "study_id")
    @JsonBackReference
    private Study study;

    @ManyToOne
    @MapsId("organizationId")
    @JoinColumn(name = "organization_id")
    @JsonBackReference
    private Organization organization;

    @Column(name = "role")
    private String role;

    @ManyToOne
    @JoinColumn(name = "responsible_personnel_id")
    private Personnel responsiblePersonnel;

    @ManyToOne
    @JoinColumn(name = "population_id")
    private Population population;
}
