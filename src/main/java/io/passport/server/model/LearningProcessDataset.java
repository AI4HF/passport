package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
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
}