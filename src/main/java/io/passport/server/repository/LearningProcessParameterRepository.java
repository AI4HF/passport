package io.passport.server.repository;

import io.passport.server.model.LearningProcessParameter;
import io.passport.server.model.LearningProcessParameterId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * LearningProcessParameter repository for database management.
 */
@Repository
public interface LearningProcessParameterRepository extends JpaRepository<LearningProcessParameter, LearningProcessParameterId> {
    List<LearningProcessParameter> findByIdLearningProcessId(Long learningProcessId);
    List<LearningProcessParameter> findByIdParameterId(Long parameterId);
}
