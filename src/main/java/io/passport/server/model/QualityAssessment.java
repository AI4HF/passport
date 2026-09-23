package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;

/**
 * One execution of a QualityCriteria set over a Dataset. Assessments recur: every time a Dataset is
 * re-extracted the criteria are re-run and a new assessment is recorded, which is what makes the
 * quality record longitudinal.
 */
@Entity
@Table(name = "quality_assessment")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "qualityAssessmentId")
public class QualityAssessment {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String qualityAssessmentId;

    @Column(name = "dataset_id")
    private String datasetId;

    @Column(name = "quality_criteria_id")
    private String qualityCriteriaId;

    @Column(name = "overall_result")
    private String overallResult;

    @Column(name = "summary")
    private String summary;

    @Column(name = "executed_at")
    private Instant executedAt;

    /**
     * The personnel or software agent that ran the assessment.
     */
    @Column(name = "executed_by")
    private String executedBy;
}
