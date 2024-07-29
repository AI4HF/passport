package io.passport.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FeatureDatasetCharacteristicId implements Serializable {
    @Column(name = "dataset_id")
    private Long datasetId;

    @Column(name = "feature_id")
    private Long featureId;
}
