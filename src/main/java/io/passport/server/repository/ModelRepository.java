package io.passport.server.repository;

import io.passport.server.model.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Model repository for database management.
 */
public interface ModelRepository extends JpaRepository<Model, String> {
    List<Model> findByStudyId(String studyId);
    List<Model> findByLearningProcessId(String learningProcessId);
    List<Model> findByExperimentId(String experimentId);
    List<Model> findByOwner(String organizationId);

    // Find Models modified by a specific Personnel
    @Query("SELECT m FROM Model m WHERE m.createdBy = :personnelId OR m.lastUpdatedBy = :personnelId")
    List<Model> findByCreatedByOrLastUpdatedBy(@Param("personnelId") String personnelId);

    // Find Study ID directly from Model ID
    @Query("SELECT m.studyId FROM Model m WHERE m.modelId = :modelId")
    Optional<String> findStudyIdByModelId(@Param("modelId") String modelId);
}
