package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Experiment model for Experiment Table.
 */
@Entity
@Table(name = "experiment")
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "experimentId")
public class Experiment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "experiment_id")
    private Long experimentId;

    @Column(name = "study_id")
    private Long studyId;

    @Column(name = "research_question")
    private String researchQuestion;
    public Experiment() {}
    public Experiment(Long experimentId, Long studyId, String researchQuestion) {
        this.experimentId = experimentId;
        this.studyId = studyId;
        this.researchQuestion = researchQuestion;
    }
}
