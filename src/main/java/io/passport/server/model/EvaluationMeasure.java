package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * EvaluationMeasure model for evaluation_measure Table
 */
@Entity
@Table(name = "evaluation_measure")
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "measureId")
public class EvaluationMeasure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "measure_id")
    private Long measureId;

    @Column(name = "model_id")
    private Long modelId;

    @Column(name = "name")
    private String name;

    @Column(name = "value")
    private String value;

    @Column(name = "data_type")
    private String dataType;

    @Column(name = "description")
    private String description;
}
