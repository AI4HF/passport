package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
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

    @Column(name = "role")
    private String role;
}
