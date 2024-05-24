package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.net.URL;

@Entity
@Table(name = "population")
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
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
