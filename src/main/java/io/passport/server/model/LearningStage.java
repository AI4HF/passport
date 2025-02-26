package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

/**
 * LearningStage model used for learning stage management tasks.
 */
@Entity
@Table(name = "learning_stage")
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "learningStageId")
public class LearningStage {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String learningStageId;

    @Column(name = "learning_process_id")
    private String learningProcessId;

    @Column(name = "learning_stage_name")
    private String learningStageName;

    @Column(name = "description")
    private String description;

    @Column(name = "dataset_percentage")
    private int datasetPercentage;
}
