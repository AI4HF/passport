package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.time.Instant;

/**
 * Dataset model used for the Dataset Management tasks.
 */
@Entity
@Table(name = "dataset")
@Getter
@Setter
public class Dataset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dataset_id")
    private Long datasetId;

    @Column(name = "featureset_id")
    private Long featuresetId;

    @Column(name = "population_id")
    private Long populationId;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "version")
    private String version;

    @Column(name = "reference_entity")
    private String referenceEntity;

    @Column(name = "num_of_records")
    private Integer numOfRecords;

    @Column(name = "synthetic")
    private Boolean synthetic;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;

    @Column(name = "last_updated_by")
    private String lastUpdatedBy;
}
