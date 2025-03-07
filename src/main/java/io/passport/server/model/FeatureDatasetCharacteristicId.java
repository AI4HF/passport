package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
public class FeatureDatasetCharacteristicId implements Serializable {
    @Column(name = "dataset_id")
    private Long datasetId;

    @Column(name = "feature_id")
    private Long featureId;
}
