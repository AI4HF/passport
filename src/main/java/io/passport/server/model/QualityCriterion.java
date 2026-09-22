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
import java.time.Instant;

/**
 * A single quality rule within a QualityCriteria set, in the feature-extraction-suite rule format:
 * "expression" is the SQL over the virtual dataset table computing one or more aliased measures,
 * "ruleExpression" the formula over those aliases, and low/high the bounds its result is checked
 * against. Categorised per the Kahn framework (completeness/conformance/plausibility).
 */
@Entity
@Table(name = "quality_criterion")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "qualityCriterionId")
public class QualityCriterion {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String qualityCriterionId;

    @Column(name = "quality_criteria_id")
    private String qualityCriteriaId;

    @Column(name = "feature_id")
    private String featureId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "context")
    private String context;

    @Column(name = "subcategory")
    private String subcategory;

    @Column(name = "language")
    private String language;

    @Column(name = "expression")
    private String expression;

    @Column(name = "rule_expression")
    private String ruleExpression;

    @Column(name = "low")
    private BigDecimal low;

    @Column(name = "high")
    private BigDecimal high;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;

    @Column(name = "last_updated_by")
    private String lastUpdatedBy;
}
