package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

/**
 * LearningProcessParameter model used for the LearningProcessParameter management.
 */
@Entity
@Table(name = "learning_process_parameter")
@Getter
@Setter
public class LearningProcessParameter implements Serializable {

    @EmbeddedId
    private LearningProcessParameterId id;

    @Column(name = "type")
    private String type;

    @Column(name = "value")
    private String value;

    /**
     * Constructs a new LearningProcessParameter from a LearningProcessParameterDTO entity.
     * @param dto the LearningProcessParameterDTO entity
     */
    public LearningProcessParameter(LearningProcessParameterDTO dto) {
        this.id = new LearningProcessParameterId();
        this.id.setLearningProcessId(dto.getLearningProcessId());
        this.id.setParameterId(dto.getParameterId());
        this.type = dto.getType();
        this.value = dto.getValue();
    }

    /**
     * Default constructor for the entity.
     */
    public LearningProcessParameter() {}
}