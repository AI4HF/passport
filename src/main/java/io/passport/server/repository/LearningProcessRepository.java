package io.passport.server.repository;

import io.passport.server.model.LearningProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * LearningProcess repository for database management.
 */
@Repository
public interface LearningProcessRepository extends JpaRepository<LearningProcess, Long> {
}
