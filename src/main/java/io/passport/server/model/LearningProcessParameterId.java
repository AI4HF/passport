package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.io.Serializable;
@Embeddable
@Getter
@Setter
public class LearningProcessParameterId implements Serializable {
    @Column(name = "learning_process_id")
    private String learningProcessId;

    @Column(name = "parameter_id")
    private String parameterId;
}