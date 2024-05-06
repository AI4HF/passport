package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;

/**
 * Study model used for the Study Management tasks.
 */
@Entity
@Table(name = "studies")
@Getter
@Setter
public class Study {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_id")
    private Long id;

    @Column(name= "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "objectives")
    private String objectives;

    @Column(name = "ethics")
    private String ethics;

    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "person_id")
    private Personnel owner;

}
