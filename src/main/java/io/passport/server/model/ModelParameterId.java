package io.passport.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.io.Serializable;
@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModelParameterId implements Serializable {
    @Column(name = "model_id")
    private String modelId;

    @Column(name = "parameter_id")
    private String parameterId;
}