package io.passport.server.repository;

import io.passport.server.model.EvaluationMeasure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * EvaluationMeasure repository for database management.
 */
public interface EvaluationMeasureRepository extends JpaRepository<EvaluationMeasure, String> {
    List<EvaluationMeasure> findAllByModelId(String modelId);
}
