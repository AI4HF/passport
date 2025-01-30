package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

/**
 * Algorithm model used for algorithm management tasks.
 */
@Entity
@Table(name = "algorithm")
@Getter
@Setter
public class Algorithm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "algorithm_id")
    private Long algorithmId;

    @Column(name = "name")
    private String name;

    @Column(name = "objective_function")
    private String objectiveFunction;

    @Column(name = "type")
    private String type;

    @Column(name = "sub_type")
    private String subType;
}
