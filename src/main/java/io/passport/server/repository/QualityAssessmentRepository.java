package io.passport.server.repository;

import io.passport.server.model.QualityAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * QualityAssessment repository for database management.
 */
@Repository
public interface QualityAssessmentRepository extends JpaRepository<QualityAssessment, String> {

    List<QualityAssessment> findByDatasetId(String datasetId);

    List<QualityAssessment> findByQualityCriteriaId(String qualityCriteriaId);

    // Join through Dataset and Population to reach the study
    @Query("SELECT qa FROM QualityAssessment qa, Dataset d, Population p " +
            "WHERE qa.datasetId = d.datasetId AND d.populationId = p.populationId AND p.studyId = :studyId")
    List<QualityAssessment> findQualityAssessmentsByStudyId(@Param("studyId") String studyId);

    // Find Study ID directly from Quality Assessment ID
    @Query("SELECT p.studyId FROM QualityAssessment qa, Dataset d, Population p " +
            "WHERE qa.datasetId = d.datasetId AND d.populationId = p.populationId " +
            "AND qa.qualityAssessmentId = :qualityAssessmentId")
    String findStudyIdByQualityAssessmentId(@Param("qualityAssessmentId") String qualityAssessmentId);
}
