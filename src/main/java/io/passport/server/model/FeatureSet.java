package io.passport.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.Instant;

/**
 * FeatureSet model used for the FeatureSet Management tasks.
 */
@Entity
@Table(name = "featureset")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
    private Instant createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;

    @Column(name = "last_updated_by")
    private String lastUpdatedBy;
}
