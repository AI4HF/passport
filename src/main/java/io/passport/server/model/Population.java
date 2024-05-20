package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.net.URL;

@Entity
@Table(name = "population")
@Getter
@Setter
public class Population {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "population_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "study_id", referencedColumnName = "study_id")
    private Study study;

    @Column(name = "populationURL")
    private URL populationURL;

    @Column(name = "description")
    private String researchQuestion;

    @Column(name = "characteristics")
    private String characteristics;


}
