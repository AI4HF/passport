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
    @Column(name = "feature_id")
    private String featureId;

    @Column(name = "dataset_id")
    private String datasetId;

    @Column(name = "characteristic_name")
    private String characteristicName;
}
