package io.passport.server.repository;

import io.passport.server.model.LearningDataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * LearningDataset repository for database management.
 */
@Repository
public interface LearningDatasetRepository extends JpaRepository<LearningDataset, Long> {
    List<LearningDataset> findByDataTransformationId(Long dataTransformationId);
    List<LearningDataset> findByDatasetId(Long datasetId);
}
