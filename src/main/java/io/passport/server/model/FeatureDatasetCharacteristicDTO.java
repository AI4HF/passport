package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeatureDatasetCharacteristicDTO {
    private Long datasetId;
    private Long featureId;
    private String characteristicName;
    private String value;
    private String valueDataType;

    public FeatureDatasetCharacteristicDTO(FeatureDatasetCharacteristic entity) {
        this.datasetId = entity.getId().getDatasetId();
        this.featureId = entity.getId().getFeatureId();
        this.characteristicName = entity.getCharacteristicName();
        this.value = entity.getValue();
        this.valueDataType = entity.getValueDataType();
    }
}

