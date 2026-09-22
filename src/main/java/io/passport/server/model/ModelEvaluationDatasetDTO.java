package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Data Transfer Object for ModelEvaluationDataset.
 */
@Getter
@Setter
public class ModelEvaluationDatasetDTO {
    private String modelEvaluationId;
    private String learningDatasetId;
    private BigDecimal weight;
    private String description;

    /**
     * Constructs a new ModelEvaluationDatasetDTO from a ModelEvaluationDataset entity.
     * @param entity the ModelEvaluationDataset entity
     */
    public ModelEvaluationDatasetDTO(ModelEvaluationDataset entity) {
        this.modelEvaluationId = entity.getId().getModelEvaluationId();
        this.learningDatasetId = entity.getId().getLearningDatasetId();
        this.weight = entity.getWeight();
        this.description = entity.getDescription();
    }

    /**
     * Default constructor created for implicit parameter initialization.
     */
    public ModelEvaluationDatasetDTO() {}
}
