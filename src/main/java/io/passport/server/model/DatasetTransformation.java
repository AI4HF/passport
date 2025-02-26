package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

/**
 * DatasetTransformation model used for the DatasetTransformation Management tasks.
 */
@Entity
@Table(name = "dataset_transformation")
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "dataTransformationId")
public class DatasetTransformation {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String dataTransformationId;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;
}
