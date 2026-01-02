package io.passport.server.repository;

import io.passport.server.model.DatasetTransformationStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * DatasetTransformationStep repository for database management.
 */
@Repository
public interface DatasetTransformationStepRepository extends JpaRepository<DatasetTransformationStep, String> {
    List<DatasetTransformationStep> findByDataTransformationId(String dataTransformationId);

    // Find Steps modified by a specific Personnel
    @Query("SELECT s FROM DatasetTransformationStep s WHERE s.createdBy = :personnelId OR s.lastUpdatedBy = :personnelId")
    List<DatasetTransformationStep> findByCreatedByOrLastUpdatedBy(@Param("personnelId") String personnelId);

    // Find Study ID directly from Transformation Step ID
    @Query("SELECT ld.studyId FROM DatasetTransformationStep s, LearningDataset ld " +
            "WHERE s.dataTransformationId = ld.dataTransformationId AND s.stepId = :stepId")
    Optional<String> findStudyIdByStepId(@Param("stepId") String stepId);
}

