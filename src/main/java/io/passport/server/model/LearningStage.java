package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

/**
 * LearningStage model used for learning stage management tasks.
 */
@Entity
@Table(name = "learning_stage")
@Getter
@Setter
public class LearningStage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "learning_stage_id")
    private Long learningStageId;

    @Column(name = "learning_process_id")
    private Long learningProcessId;

    @Column(name = "learning_stage_name")
    private String learningStageName;

    @Column(name = "description")
    private String description;

    @Column(name = "dataset_percentage")
    private int datasetPercentage;
}
