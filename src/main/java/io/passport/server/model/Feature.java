package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;

/**
 * Feature model used for the Feature Management tasks.
 */
@Entity
@Table(name = "feature")
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "featureId")
public class Feature {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String featureId;

    @Column(name = "featureset_id")
    private String featuresetId;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "data_type")
    private String dataType;

    @Column(name = "feature_type")
    private String featureType;

    @Column(name = "mandatory")
    private Boolean mandatory;

    @Column(name = "isUnique")
    private Boolean isUnique;

    @Column(name = "units")
    private String units;

    @Column(name = "equipment")
    private String equipment;

    @Column(name = "data_collection")
    private String dataCollection;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;

    @Column(name = "last_updated_by")
    private String lastUpdatedBy;
}
