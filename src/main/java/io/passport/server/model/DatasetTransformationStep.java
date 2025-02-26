package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;

/**
 * DatasetTransformationStep model used for the DatasetTransformationStep Management tasks.
 */
@Entity
@Table(name = "dataset_transformation_step")
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "stepId")
public class DatasetTransformationStep {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String stepId;

    @Column(name = "data_transformation_id")
    private String dataTransformationId;

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
