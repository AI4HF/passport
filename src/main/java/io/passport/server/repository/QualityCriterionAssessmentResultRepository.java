package io.passport.server.repository;

import io.passport.server.model.QualityCriterionAssessmentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * QualityCriterionAssessmentResult repository for database management.
 */
@Repository
public interface QualityCriterionAssessmentResultRepository
        extends JpaRepository<QualityCriterionAssessmentResult, String> {

    List<QualityCriterionAssessmentResult> findByQualityAssessmentId(String qualityAssessmentId);

    List<QualityCriterionAssessmentResult> findByQualityCriterionId(String qualityCriterionId);
}
