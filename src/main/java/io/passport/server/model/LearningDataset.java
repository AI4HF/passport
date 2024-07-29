package io.passport.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;

/**
 * LearningDataset model used for the LearningDataset Management tasks.
 */
@Entity
@Table(name = "learning_dataset")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LearningDataset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "learning_dataset_id")
    private Long learningDatasetId;

    @Column(name = "dataset_id")
    private Long datasetId;

    @Column(name = "data_transformation_id")
    private Long dataTransformationId;

    @Column(name = "description")
    private String description;
}
