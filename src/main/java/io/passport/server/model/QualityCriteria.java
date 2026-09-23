package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;

/**
 * A set of data quality criteria for an Experiment, authored in the feature-extraction-suite
 * datasetqualitycriteria format and executed against each extracted Dataset. (url, version) is the
 * canonical identity of the definition: a new version is a new row, never an update.
 */
@Entity
@Table(name = "quality_criteria")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "qualityCriteriaId")
public class QualityCriteria {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String qualityCriteriaId;

    @Column(name = "experiment_id")
    private String experimentId;

    @Column(name = "title")
    private String title;

    @Column(name = "url")
    private String url;

    @Column(name = "description")
    private String description;

    @Column(name = "version")
    private String version;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;

    @Column(name = "last_updated_by")
    private String lastUpdatedBy;
}
