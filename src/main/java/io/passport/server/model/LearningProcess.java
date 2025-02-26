package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

/**
 * LearningProcess model used for learning process management tasks.
 */
@Entity
@Table(name = "learning_process")
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "learningProcessId")
public class LearningProcess {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String learningProcessId;

    @Column(name = "study_id")
    private String studyId;

    @Column(name = "implementation_id")
    private String implementationId;

    @Column(name = "description")
    private String description;
}
