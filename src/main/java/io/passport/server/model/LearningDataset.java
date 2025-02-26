package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

/**
 * LearningDataset model used for the LearningDataset Management tasks.
 */
@Entity
@Table(name = "learning_dataset")
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "learningDatasetId")
public class LearningDataset {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String learningDatasetId;

    @Column(name = "dataset_id")
    private String datasetId;

    @Column(name = "study_id")
    private String studyId;

    @Column(name = "data_transformation_id")
    private String dataTransformationId;

    @Column(name = "description")
    private String description;
}
