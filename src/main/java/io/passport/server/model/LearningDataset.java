package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

/**
 * LearningDataset model used for the LearningDataset Management tasks.
 */
@Entity
@Table(name = "learning_dataset")
@Getter
@Setter
public class LearningDataset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "learning_dataset_id")
    private Long learningDatasetId;

    @Column(name = "dataset_id")
    private Long datasetId;

    @Column(name = "study_id")
    private Long studyId;

    @Column(name = "data_transformation_id")
    private Long dataTransformationId;

    @Column(name = "description")
    private String description;
}
