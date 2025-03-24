package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

/**
 * Implementation model used for implementation management tasks.
 */
@Entity
@Table(name = "implementation")
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "implementationId")
public class Implementation {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String implementationId;

    @Column(name = "algorithm_id")
    private String algorithmId;

    @Column(name = "software")
    private String software;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;
}
