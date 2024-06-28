package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;

/**
 * Study model used for the Study Management tasks.
 */
@Entity
@Table(name = "study")
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

    @Column(name = "owner")
    private Long owner;

    public Study() {}
    public Study(Long id, String name, String description, String objectives, String ethics, Long owner) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.objectives = objectives;
        this.ethics = ethics;
        this.owner = owner;
    }
}
