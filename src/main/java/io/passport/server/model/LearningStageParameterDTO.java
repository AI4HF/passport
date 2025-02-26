package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for LearningStageParameter.
 */
@Getter
@Setter
public class LearningStageParameterDTO {
    private String learningStageId;
    private String parameterId;
    private String type;
    private String value;

    /**
     * Constructs a new LearningStageParameterDTO from a LearningStageParameter entity.
     * @param entity the LearningStageParameter entity
     */
    public LearningStageParameterDTO(LearningStageParameter entity) {
        this.learningStageId = entity.getId().getLearningStageId();
        this.parameterId = entity.getId().getParameterId();
        this.type = entity.getType();
        this.value = entity.getValue();
    }

    /**
     * Default constructor created for implicit parameter initialization.
     */
    public LearningStageParameterDTO() {}
}
