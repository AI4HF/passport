package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;

import jakarta.persistence.*;

/**
 * Personnel model for later use, implemented early to implement Study structure properly.
 */
@Entity
@Table(name = "personnel")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "personId")
public class Personnel {
    @Id
    @Column(name = "person_id")
    private String personId;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

}
