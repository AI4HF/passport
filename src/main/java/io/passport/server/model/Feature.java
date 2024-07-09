package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Feature model used for the Feature Management tasks.
 */
@Entity
@Table(name = "feature")
@Getter
@Setter
public class Feature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feature_id")
    private Long featureId;

    @Column(name = "featureset_id")
    private Long featuresetId;

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
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @Column(name = "last_updated_by")
    private Long lastUpdatedBy;
}
