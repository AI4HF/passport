package io.passport.server.model.composite_keys;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class StudyPersonnelKey implements Serializable {

    @Column(name = "study_id")
    private Long studyId;

    @Column(name = "personnel_id")
    private Long personnelId;
}

