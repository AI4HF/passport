package io.passport.server.repository;

import io.passport.server.model.Feature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Feature repository for database management.
 */
@Repository
public interface FeatureRepository extends JpaRepository<Feature, String> {
    List<Feature> findByFeaturesetId(String featuresetId);

    // Find Features modified by a specific Personnel
    @Query("SELECT f FROM Feature f WHERE f.createdBy = :personnelId OR f.lastUpdatedBy = :personnelId")
    List<Feature> findByCreatedByOrLastUpdatedBy(@Param("personnelId") String personnelId);

    // Find Study ID directly from Feature ID
    @Query("SELECT e.studyId FROM Feature f, FeatureSet fs, Experiment e " +
            "WHERE f.featuresetId = fs.featuresetId AND fs.experimentId = e.experimentId AND f.featureId = :featureId")
    Optional<String> findStudyIdByFeatureId(@Param("featureId") String featureId);
}
