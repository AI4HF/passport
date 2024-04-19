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
    private Long id;

    @Column(name = "study_id")
    private String studyId;

    private String name;

    private String description;

    private String objectives;

    private String ethics;

    @ManyToOne
    @JoinColumn(name = "owner")
    private Personnel owner;
}
