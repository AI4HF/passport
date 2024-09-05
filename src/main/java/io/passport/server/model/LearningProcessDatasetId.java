package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
public class LearningProcessDatasetId implements Serializable {
    @Column(name = "learning_process_id")
    private Long learningProcessId;

    @Column(name = "learning_dataset_id")
    private Long learningDatasetId;
}