package io.passport.server.repository;

import io.passport.server.model.QualityCriterion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * QualityCriterion repository for database management.
 */
@Repository
public interface QualityCriterionRepository extends JpaRepository<QualityCriterion, String> {

    List<QualityCriterion> findByQualityCriteriaId(String qualityCriteriaId);

    List<QualityCriterion> findByFeatureId(String featureId);

    // Find Study ID through the owning criteria set and its experiment
    @Query("SELECT e.studyId FROM QualityCriterion c, QualityCriteria qc, Experiment e " +
            "WHERE c.qualityCriteriaId = qc.qualityCriteriaId AND qc.experimentId = e.experimentId " +
            "AND c.qualityCriterionId = :qualityCriterionId")
    String findStudyIdByQualityCriterionId(@Param("qualityCriterionId") String qualityCriterionId);

    // Find Quality Criteria modified by a specific Personnel
    @Query("SELECT c FROM QualityCriterion c WHERE c.createdBy = :personnelId OR c.lastUpdatedBy = :personnelId")
    List<QualityCriterion> findByCreatedByOrLastUpdatedBy(@Param("personnelId") String personnelId);
}
