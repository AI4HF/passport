package io.passport.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;

/**
 * DatasetTransformation model used for the DatasetTransformation Management tasks.
 */
@Entity
@Table(name = "dataset_transformation")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
