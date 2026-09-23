package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

/**
 * A software component that creates or updates records in the Passport through an automated
 * integration - the node agent's dataset sync, its metadata publisher and monitoring import, or
 * FL Central pushing model metadata through the Python client library. Each agent authenticates
 * with its own Keycloak service account; {@code keycloakClientId} binds that account to this row,
 * so an automated write can be attributed in the audit log.
 */
@Entity
@Table(name = "software_agent")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "softwareAgentId")
public class SoftwareAgent {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String softwareAgentId;

    @Column(name = "name")
    private String name;

    @Column(name = "version")
    private String version;

    @Column(name = "description")
    private String description;

    @Column(name = "keycloak_client_id")
    private String keycloakClientId;
}
