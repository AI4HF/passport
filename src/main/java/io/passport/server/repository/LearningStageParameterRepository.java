package io.passport.server.repository;

import io.passport.server.model.LearningStageParameter;
import io.passport.server.model.LearningStageParameterId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * LearningStageParameter repository for database management.
 */
@Repository
public interface LearningStageParameterRepository extends JpaRepository<LearningStageParameter, LearningStageParameterId> {
    List<LearningStageParameter> findByIdLearningStageId(String learningStageId);
    List<LearningStageParameter> findByIdParameterId(String parameterId);
}

