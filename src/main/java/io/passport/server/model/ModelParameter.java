package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * ModelParameter model used for the ModelParameter management.
 */
@Entity
@Table(name = "model_parameter")
@Getter
@Setter
public class ModelParameter implements Serializable {

    @EmbeddedId
    private ModelParameterId id;

    @Column(name = "type")
    private String type;

    @Column(name = "value")
    private String value;

    /**
     * Constructs a new ModelParameter from a ModelParameterDTO entity.
     * @param dto the ModelParameterDTO entity
     */
    public ModelParameter(ModelParameterDTO dto) {
        this.id = new ModelParameterId();
        this.id.setModelId(dto.getModelId());
        this.id.setParameterId(dto.getParameterId());
        this.type = dto.getType();
        this.value = dto.getValue();
    }

    /**
     * Default constructor for the entity.
     */
    public ModelParameter() {}
}