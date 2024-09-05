package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for LearningProcessDataset.
 */
@Getter
@Setter
public class LearningProcessDatasetDTO {
    private Long learningProcessId;
    private Long learningDatasetId;
    private String description;

    /**
     * Constructs a new LearningProcessDatasetDTO from a LearningProcessDataset entity.
     * @param entity the LearningProcessDataset entity
     */
    public LearningProcessDatasetDTO(LearningProcessDataset entity) {
        this.learningProcessId = entity.getId().getLearningProcessId();
        this.learningDatasetId = entity.getId().getLearningDatasetId();
        this.description = entity.getDescription();
    }

    /**
     * Default constructor created for implicit parameter initialization.
     */
    public LearningProcessDatasetDTO() {}
}
