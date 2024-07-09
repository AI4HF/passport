package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * FeatureSet model used for the FeatureSet Management tasks.
 */
@Entity
@Table(name = "featureset")
@Getter
@Setter
public class FeatureSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "featureset_id")
    private Long featuresetId;

    @Column(name = "experiment_id")
    private Long experimentId;

    @Column(name = "title")
    private String title;

    @Column(name = "featureset_url")
    private String featuresetURL;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @Column(name = "last_updated_by")
    private Long lastUpdatedBy;
}
