package io.passport.server.repository;

import io.passport.server.model.ModelEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * ModelEvaluation repository for database management.
 */
public interface ModelEvaluationRepository extends JpaRepository<ModelEvaluation, String> {
    List<ModelEvaluation> findByModelId(String modelId);
    List<ModelEvaluation> findByOrganizationId(String organizationId);
}
