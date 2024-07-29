package io.passport.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.time.Instant;

/**
 * DatasetTransformationStep model used for the DatasetTransformationStep Management tasks.
 */
@Entity
@Table(name = "dataset_transformation_step")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
    private Instant createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;

    @Column(name = "last_updated_by")
    private String lastUpdatedBy;
}
