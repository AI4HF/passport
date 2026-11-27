package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Dataset model used for the Dataset Management tasks.
 */
@Entity
@Table(name = "dataset")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "datasetId")
public class Dataset {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String datasetId;

    @Column(name = "featureset_id")
    private String featuresetId;

    @Column(name = "population_id")
    private String populationId;

    @Column(name = "organization_id")
    private String organizationId;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "version")
    private String version;

    @Column(name = "reference_entity")
    private String referenceEntity;

    @Column(name = "number_of_records")
    private Integer numberOfRecords;

    @Column(name = "synthetic")
    private Boolean synthetic;

    /**
     * The dataset version this one refreshed, if any. A re-extraction is a new row in the chain.
     */
    @Column(name = "previous_dataset_id")
    private String previousDatasetId;

    @Column(name = "persistent_identifier")
    private String persistentIdentifier;

    @Column(name = "structured_data")
    private Boolean structuredData;

    @Column(name = "temporal_coverage_start")
    private LocalDate temporalCoverageStart;

    @Column(name = "temporal_coverage_end")
    private LocalDate temporalCoverageEnd;

    @Column(name = "temporal_resolution")
    private String temporalResolution;

    @Column(name = "geographical_coverage")
    private String geographicalCoverage;

    @Column(name = "number_of_unique_individuals")
    private Integer numberOfUniqueIndividuals;

    @Column(name = "min_typical_age")
    private Integer minTypicalAge;

    @Column(name = "max_typical_age")
    private Integer maxTypicalAge;

    @Column(name = "conforms_to")
    private String conformsTo;

    @Column(name = "provenance_statement")
    private String provenanceStatement;

    @Column(name = "was_generated_by")
    private String wasGeneratedBy;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;

    @Column(name = "last_updated_by")
    private String lastUpdatedBy;
}
