package io.passport.server.model;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

/**
 * DeploymentEnvironment model on which ModelDeployment will be performed.
 */
@Entity
@Table(name = "deployment_environment")
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "environmentId")
public class DeploymentEnvironment {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String environmentId;

    @Column(name = "title")
    private String title;

    @Column(name = "description", columnDefinition="text")
    private String description;

    @Column(name = "hardware_properties", columnDefinition="text")
    private String hardwareProperties;

    @Column(name = "software_properties", columnDefinition="text")
    private String softwareProperties;

    @Column(name = "connectivity_details", columnDefinition="text")
    private String connectivityDetails;
}
