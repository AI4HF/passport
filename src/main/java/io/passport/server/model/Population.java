package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Table(name = "population")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "populationId")
public class Population {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "population_id")
    private Long populationId;

    @Column(name = "study_id")
    private Long studyId;

    @Column(name = "population_url")
    private String populationUrl;

    @Column(name = "description")
    private String description;

    @Column(name = "characteristics")
    private String characteristics;


}
