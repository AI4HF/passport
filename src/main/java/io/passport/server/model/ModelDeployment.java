package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;

/**
 * ModelDeployment model used for the Deployment Management tasks.
 */
@Entity
@Table(name = "model_deployment")
@Getter
@Setter
public class ModelDeployment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deployment_id")
    private Long deploymentId;

    @Column(name= "model_id")
    private Long modelId;

    @Column(name = "environment_id")
    private Long environmentId;

    @Column(name = "tags")
    private String tags;

    @Column(name = "identified_failures")
    private String identifiedFailures;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;

    @Column(name = "last_updated_by")
    private Long lastUpdatedBy;

}
