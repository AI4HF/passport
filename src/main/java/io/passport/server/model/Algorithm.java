package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

/**
 * Algorithm model used for algorithm management tasks.
 */
@Entity
@Table(name = "algorithm")
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "algorithmId")
public class Algorithm {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String algorithmId;

    @Column(name = "name")
    private String name;

    @Column(name = "objective_function")
    private String objectiveFunction;

    @Column(name = "type")
    private String type;

    @Column(name = "sub_type")
    private String subType;
}
