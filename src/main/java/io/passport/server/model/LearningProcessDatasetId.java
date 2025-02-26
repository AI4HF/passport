package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
public class LearningProcessDatasetId implements Serializable {
    @Column(name = "learning_process_id")
    private String learningProcessId;

    @Column(name = "learning_dataset_id")
    private String learningDatasetId;
}