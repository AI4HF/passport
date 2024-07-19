package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeatureDatasetCharacteristicDTO {
    private Long datasetId;
    private Long featureId;
    private String characteristicName;
    private Double value;
    private String valueDataType;
}
