package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * DatasetTransformationStep model used for the DatasetTransformationStep Management tasks.
 */
@Entity
@Table(name = "dataset_transformation_step")
@Getter
@Setter
public class DatasetTransformationStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "step_id")
    private Long stepId;

    @Column(name = "data_transformation_id")
    private Long dataTransformationId;

    @Column(name = "input_features")
    private String inputFeatures;

    @Column(name = "output_features")
    private String outputFeatures;

    @Column(name = "method")
    private String method;

    @Column(name = "explanation")
    private String explanation;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @Column(name = "last_updated_by")
    private Long lastUpdatedBy;
}
