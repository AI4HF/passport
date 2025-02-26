package io.passport.server.repository;

import io.passport.server.model.LearningProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * LearningProcess repository for database management.
 */
@Repository
public interface LearningProcessRepository extends JpaRepository<LearningProcess, String> {

    List<LearningProcess> findAllByStudyId(String studyId);
}
