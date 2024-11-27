package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

/**
 * DatasetTransformation model used for the DatasetTransformation Management tasks.
 */
@Entity
@Table(name = "dataset_transformation")
@Getter
@Setter
public class DatasetTransformation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "data_transformation_id")
    private Long dataTransformationId;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;
}
