package io.passport.server.model;

import javax.persistence.*;

public class Survey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "survey_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "study_id", referencedColumnName = "study_id")
    private Study study;

    @Column(name = "question")
    private String question;

    @Column(name = "answer")
    private String answer;

    @Column(name = "category")
    private String category;
}
