package io.passport.server.repository;

import io.passport.server.model.LearningStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * LearningStage repository for database management.
 */
@Repository
public interface LearningStageRepository extends JpaRepository<LearningStage, String> {
    List<LearningStage> findByLearningProcessId(String learningProcessId);
}
