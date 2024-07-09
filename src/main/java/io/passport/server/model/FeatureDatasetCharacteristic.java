package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

/**
 * FeatureDatasetCharacteristic model used for the FeatureDatasetCharacteristic management.
 */
@Entity
@Table(name = "feature_dataset_characteristic")
@Getter
@Setter
public class FeatureDatasetCharacteristic implements Serializable {

    @EmbeddedId
    private FeatureDatasetCharacteristicId id;

    @Column(name = "characteristic_name")
    private String characteristicName;

    @Column(name = "value")
    private Double value;

    @Column(name = "value_data_type")
    private String valueDataType;
}
