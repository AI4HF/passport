package io.passport.server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModelEvaluationDatasetId implements Serializable {
    @Column(name = "model_evaluation_id")
    private String modelEvaluationId;

    @Column(name = "learning_dataset_id")
    private String learningDatasetId;
}
