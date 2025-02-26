package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

/**
 * Parameter model for Parameter Table
 */
@Entity
@Table(name = "parameter")
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "parameterId")
public class Parameter {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String parameterId;

    @Column(name = "study_id")
    private String studyId;

    @Column(name = "name")
    private String name;

    @Column(name = "data_type")
    private String dataType;


    @Column(name = "description")
    private String description;

}
