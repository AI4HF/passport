package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * LearningProcessDataset model used for the LearningProcessDataset management.
 */
@Entity
@Table(name = "learning_process_dataset")
@Getter
@Setter
public class LearningProcessDataset implements Serializable {

    @EmbeddedId
    private LearningProcessDatasetId id;

    @Column(name = "description")
    private String description;

    /**
     * Constructs a new LearningProcessDataset from a LearningProcessDatasetDTO entity.
     * @param dto the LearningProcessDatasetDTO entity
     */
    public LearningProcessDataset(LearningProcessDatasetDTO dto) {
        this.id = new LearningProcessDatasetId();
        this.id.setLearningProcessId(dto.getLearningProcessId());
        this.id.setLearningDatasetId(dto.getLearningDatasetId());
        this.description = dto.getDescription();
    }

    /**
     * Default constructor for the entity.
     */
    public LearningProcessDataset() {}
}