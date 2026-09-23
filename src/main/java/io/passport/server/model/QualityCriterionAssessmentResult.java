package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;

/**
 * The outcome of a single QualityCriterion within a QualityAssessment run: the computed value
 * alongside its pass/fail result, so values can be tracked across runs.
 */
@Entity
@Table(name = "quality_criterion_assessment_result")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "resultId")
public class QualityCriterionAssessmentResult {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String resultId;

    @Column(name = "quality_assessment_id")
    private String qualityAssessmentId;

    @Column(name = "quality_criterion_id")
    private String qualityCriterionId;

    @Column(name = "value")
    private BigDecimal value;

    @Column(name = "result")
    private String result;

    @Column(name = "detail")
    private String detail;
}
