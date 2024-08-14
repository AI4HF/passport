package io.passport.server.repository;

import io.passport.server.model.LearningStage;
import io.passport.server.model.LearningStageParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * LearningStage repository for database management.
 */
@Repository
public interface LearningStageRepository extends JpaRepository<LearningStage, Long> {
    List<LearningStage> findByLearningProcessId(Long learningProcessId);
}
