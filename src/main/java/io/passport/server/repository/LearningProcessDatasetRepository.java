package io.passport.server.repository;

import io.passport.server.model.LearningProcessDataset;
import io.passport.server.model.LearningProcessDatasetId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * LearningProcessDataset repository for database management.
 */
@Repository
public interface LearningProcessDatasetRepository extends JpaRepository<LearningProcessDataset, LearningProcessDatasetId> {
    List<LearningProcessDataset> findByIdLearningProcessId(String learningProcessId);
    List<LearningProcessDataset> findByIdLearningDatasetId(String learningDatasetId);
}

