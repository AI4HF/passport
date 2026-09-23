package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;

/**
 * One evaluation run of a Model. Two kinds of runs are recorded: the aggregated training or retraining
 * evaluation pushed by FL Central over the per-node LearningDatasets, and - at retraining time only - the
 * deployment-period statistics of the model being replaced, imported from the Monitoring Platform per
 * organization. Per-site results never leave the node in any other case, so a run carries an
 * organizationId only when it is attributable to a single site.
 */
@Entity
@Table(name = "model_evaluation")
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "modelEvaluationId")
public class ModelEvaluation {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "model_evaluation_id")
    private String modelEvaluationId;

    @Column(name = "model_id")
    private String modelId;

    /**
     * The organization the run is attributable to. Training and retraining evaluations are inherently
     * cross-site and leave this null; a monitoring import sets it.
     */
    @Column(name = "organization_id")
    private String organizationId;

    @Column(name = "trigger")
    private String trigger;

    @Column(name = "aggregation_method")
    private String aggregationMethod;

    @Column(name = "executed_at")
    private Instant executedAt;

    @Column(name = "executed_by")
    private String executedBy;

    @Column(name = "description")
    private String description;
}
