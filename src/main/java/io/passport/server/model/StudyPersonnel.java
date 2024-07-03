package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

/**
 * StudyPersonnel model used for the StudyPersonnel management.
 */
@Entity
@Table(name = "study_personnel")
@Getter
@Setter
public class StudyPersonnel implements Serializable {

    @EmbeddedId
    private StudyPersonnelId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;
}
