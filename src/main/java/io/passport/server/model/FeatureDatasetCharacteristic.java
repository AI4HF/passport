package io.passport.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@AllArgsConstructor
@NoArgsConstructor
public class FeatureDatasetCharacteristic implements Serializable {

    @EmbeddedId
    private FeatureDatasetCharacteristicId id;

    @Column(name = "characteristic_name")
    private String characteristicName;

    @Column(name = "value")
    private String value;

    @Column(name = "value_data_type")
    private String valueDataType;

    /**
     * Constructs a new FeatureDatasetCharacteristic from a FeatureDatasetCharacteristicDTO entity.
     * @param dto the FeatureDatasetCharacteristicDTO entity
     */
    public FeatureDatasetCharacteristic(FeatureDatasetCharacteristicDTO dto) {
        this.id = new FeatureDatasetCharacteristicId();
        this.id.setDatasetId(dto.getDatasetId());
        this.id.setFeatureId(dto.getFeatureId());
        this.characteristicName = dto.getCharacteristicName();
        this.value = dto.getValue();
        this.valueDataType = dto.getValueDataType();
    }

    /**
     * Default constructor for the entity.
     */
    public FeatureDatasetCharacteristic() {}
}
