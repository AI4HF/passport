package io.passport.server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * A LearningDataset a ModelEvaluation was computed over. A federated aggregate links one row per
 * participating organization, which keeps the multi-site traceability without publishing per-site
 * results; a monitoring import has no rows here, since it is computed over live inference data.
 */
@Entity
@Table(name = "model_evaluation_dataset")
@Getter
@Setter
public class ModelEvaluationDataset implements Serializable {

    @EmbeddedId
    private ModelEvaluationDatasetId id;

    /**
     * The dataset's share of the aggregate, for weighted aggregation methods.
     */
    @Column(name = "weight")
    private BigDecimal weight;

    @Column(name = "description")
    private String description;

    /**
     * Constructs a new ModelEvaluationDataset from a ModelEvaluationDatasetDTO entity.
     * @param dto the ModelEvaluationDatasetDTO entity
     */
    public ModelEvaluationDataset(ModelEvaluationDatasetDTO dto) {
        this.id = new ModelEvaluationDatasetId();
        this.id.setModelEvaluationId(dto.getModelEvaluationId());
        this.id.setLearningDatasetId(dto.getLearningDatasetId());
        this.weight = dto.getWeight();
        this.description = dto.getDescription();
    }

    /**
     * Default constructor for the entity.
     */
    public ModelEvaluationDataset() {}
}
