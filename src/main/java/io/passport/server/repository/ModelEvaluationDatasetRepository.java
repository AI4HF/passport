package io.passport.server.repository;

import io.passport.server.model.ModelEvaluationDataset;
import io.passport.server.model.ModelEvaluationDatasetId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ModelEvaluationDataset repository for database management.
 */
@Repository
public interface ModelEvaluationDatasetRepository extends JpaRepository<ModelEvaluationDataset, ModelEvaluationDatasetId> {
    List<ModelEvaluationDataset> findByIdModelEvaluationId(String modelEvaluationId);
    List<ModelEvaluationDataset> findByIdLearningDatasetId(String learningDatasetId);
}
