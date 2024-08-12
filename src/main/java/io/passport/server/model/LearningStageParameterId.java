package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
@Embeddable
@Getter
@Setter
public class LearningStageParameterId implements Serializable {
    @Column(name = "learning_stage_id")
    private Long learningStageId;

    @Column(name = "parameter_id")
    private Long parameterId;
}