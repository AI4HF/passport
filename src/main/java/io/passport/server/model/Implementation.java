package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

/**
 * Implementation model used for implementation management tasks.
 */
@Entity
@Table(name = "implementation")
@Getter
@Setter
public class Implementation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "implementation_id")
    private Long implementationId;

    @Column(name = "algorithm_id")
    private Long algorithmId;

    @Column(name = "software")
    private String software;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;
}
