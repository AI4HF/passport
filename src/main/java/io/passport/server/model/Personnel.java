package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;

/**
 * Personnel model for later use, implemented early to implement Study structure properly.
 */
@Entity
@Table(name = "personnel")
@Getter
@Setter
public class Personnel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "person_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "role")
    private String role;

    @Column(name = "email")
    private String email;
}
