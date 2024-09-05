package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for LearningProcessParameter.
 */
@Getter
@Setter
public class LearningProcessParameterDTO {
    private Long learningProcessId;
    private Long parameterId;
    private String type;
    private String value;

    /**
     * Constructs a new LearningProcessParameterDTO from a LearningProcessParameter entity.
     * @param entity the LearningProcessParameter entity
     */
    public LearningProcessParameterDTO(LearningProcessParameter entity) {
        this.learningProcessId = entity.getId().getLearningProcessId();
        this.parameterId = entity.getId().getParameterId();
        this.type = entity.getType();
        this.value = entity.getValue();
    }

    /**
     * Default constructor created for implicit parameter initialization.
     */
    public LearningProcessParameterDTO() {}
}
