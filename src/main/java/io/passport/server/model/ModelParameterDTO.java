package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for ModelParameter.
 */
@Getter
@Setter
public class ModelParameterDTO {
    private String modelId;
    private String parameterId;
    private String type;
    private String value;

    /**
     * Constructs a new ModelParameterDTO from a ModelParameter entity.
     * @param entity the ModelParameter entity
     */
    public ModelParameterDTO(ModelParameter entity) {
        this.modelId = entity.getId().getModelId();
        this.parameterId = entity.getId().getParameterId();
        this.type = entity.getType();
        this.value = entity.getValue();
    }

    /**
     * Default constructor created for implicit parameter initialization.
     */
    public ModelParameterDTO() {}
}