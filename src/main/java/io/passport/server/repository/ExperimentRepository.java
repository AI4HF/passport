package io.passport.server.repository;

import io.passport.server.model.Experiment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Experiment repository for database management.
 */
public interface ExperimentRepository extends JpaRepository<Experiment, Long> {
    List<Experiment> findByStudyId(Long studyId);
    void deleteAllByStudyId(Long studyId);
}
