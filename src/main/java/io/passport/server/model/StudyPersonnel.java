package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.passport.server.model.composite_keys.StudyPersonnelKey;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "study_personnel")
@Getter
@Setter
public class StudyPersonnel implements Serializable {

    @EmbeddedId
    private StudyPersonnelKey id;

    @ManyToOne
    @MapsId("studyId")
    @JoinColumn(name = "study_id")
    @JsonBackReference
    private Study study;

    @ManyToOne
    @MapsId("personnelId")
    @JoinColumn(name = "personnel_id")
    @JsonBackReference
    private Personnel personnel;

    @Column(name = "role")
    private String role;
}
