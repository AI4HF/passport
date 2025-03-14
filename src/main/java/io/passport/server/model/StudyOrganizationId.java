package io.passport.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StudyOrganizationId implements Serializable {
    @Column(name = "organization_id")
    private String organizationId;

    @Column(name = "study_id")
    private String studyId;
}
