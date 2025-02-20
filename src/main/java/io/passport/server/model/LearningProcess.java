package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

/**
 * LearningProcess model used for learning process management tasks.
 */
@Entity
@Table(name = "learning_process")
@Getter
@Setter
public class LearningProcess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "learning_process_id")
    private Long learningProcessId;

    @Column(name = "study_id")
    private Long studyId;

    @Column(name = "implementation_id")
    private Long implementationId;

    @Column(name = "description")
    private String description;
}
