package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
public class StudyPersonnelId implements Serializable {
    @Column(name = "study_id")
    private Long studyId;

    @Column(name = "personnel_id")
    private String personnelId;
}
