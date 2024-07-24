package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Learning Dataset and Dataset Transformation Data Transfer Object used for handling composite request bodies.
 */
@Getter
@Setter
public class LearningDatasetandTransformationDTO {
    private DatasetTransformation datasetTransformation;
    private LearningDataset learningDataset;

    public LearningDatasetandTransformationDTO(DatasetTransformation transformation, LearningDataset learningDataset) {
        this.learningDataset = learningDataset;
        this.datasetTransformation = transformation;
    }
}
