package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "experiment")
@Getter
@Setter
public class Experiment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "experiment_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "study_id", referencedColumnName = "study_id")
    private Study study;

    @Column(name = "researchQuestion")
    private String researchQuestion;
}
