package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

/**
 * LearningStageParameter model used for the LearningStageParameter management.
 */
@Entity
@Table(name = "learning_stage_parameter")
@Getter
@Setter
public class LearningStageParameter implements Serializable {

    @EmbeddedId
    private LearningStageParameterId id;

    @Column(name = "type")
    private String type;

    @Column(name = "value")
    private String value;

    /**
     * Constructs a new LearningStageParameter from a LearningStageParameterDTO entity.
     * @param dto the LearningStageParameterDTO entity
     */
    public LearningStageParameter(LearningStageParameterDTO dto) {
        this.id = new LearningStageParameterId();
        this.id.setLearningStageId(dto.getLearningStageId());
        this.id.setParameterId(dto.getParameterId());
        this.type = dto.getType();
        this.value = dto.getValue();
    }

    /**
     * Default constructor for the entity.
     */
    public LearningStageParameter() {}
}