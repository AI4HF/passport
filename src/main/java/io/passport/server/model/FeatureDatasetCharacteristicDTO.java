package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for FeatureDatasetCharacteristic.
 */
@Getter
@Setter
public class FeatureDatasetCharacteristicDTO {
    private String datasetId;
    private String featureId;
    private String characteristicName;
    private String value;
    private String valueDataType;

    /**
     * Constructs a new FeatureDatasetCharacteristicDTO from a FeatureDatasetCharacteristic entity.
     * @param entity the FeatureDatasetCharacteristic entity
     */
    public FeatureDatasetCharacteristicDTO(FeatureDatasetCharacteristic entity) {
        this.datasetId = entity.getId().getDatasetId();
        this.featureId = entity.getId().getFeatureId();
        this.characteristicName = entity.getCharacteristicName();
        this.value = entity.getValue();
        this.valueDataType = entity.getValueDataType();
    }

    /**
     * Default constructor created for implicit parameter initialization.
     */
    public FeatureDatasetCharacteristicDTO(){}
}

