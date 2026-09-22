package io.passport.server.repository;

import io.passport.server.model.QualityCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * QualityCriteria repository for database management.
 */
@Repository
public interface QualityCriteriaRepository extends JpaRepository<QualityCriteria, String> {

    List<QualityCriteria> findByExperimentId(String experimentId);

    // Join with experiment table and get related quality criteria for the study
    @Query("SELECT qc FROM QualityCriteria qc, Experiment e " +
            "WHERE qc.experimentId = e.experimentId AND e.studyId = :studyId")
    List<QualityCriteria> findQualityCriteriaByStudyId(@Param("studyId") String studyId);

    // Find Study ID directly from Quality Criteria ID
    @Query("SELECT e.studyId FROM QualityCriteria qc, Experiment e " +
            "WHERE qc.experimentId = e.experimentId AND qc.qualityCriteriaId = :qualityCriteriaId")
    String findStudyIdByQualityCriteriaId(@Param("qualityCriteriaId") String qualityCriteriaId);

    // Find Quality Criteria modified by a specific Personnel
    @Query("SELECT qc FROM QualityCriteria qc WHERE qc.createdBy = :personnelId OR qc.lastUpdatedBy = :personnelId")
    List<QualityCriteria> findByCreatedByOrLastUpdatedBy(@Param("personnelId") String personnelId);
}
