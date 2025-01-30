package io.passport.server.model;


import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

/**
 * DeploymentEnvironment model on which ModelDeployment will be performed.
 */
@Entity
@Table(name = "deployment_environment")
@Getter
@Setter
public class DeploymentEnvironment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "environment_id")
    private Long environmentId;

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
