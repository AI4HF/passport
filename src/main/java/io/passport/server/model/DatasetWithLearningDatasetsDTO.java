package io.passport.server.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
/**
 * DTO that encapsulates a Dataset and its associated LearningDatasets.
 */
public class DatasetWithLearningDatasetsDTO {

    /**
     * The Dataset entity.
     */
    private Dataset dataset;

    /**
     * The list of LearningDatasets associated with the Dataset.
     */
    private List<LearningDataset> learningDatasets;

    // Getters and Setters

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public List<LearningDataset> getLearningDatasets() {
        return learningDatasets;
    }

    public void setLearningDatasets(List<LearningDataset> learningDatasets) {
        this.learningDatasets = learningDatasets;
    }
}
