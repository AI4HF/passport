package io.passport.server.repository;

import io.passport.server.model.Experiment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * Experiment repository for database management.
 */
public interface ExperimentRepository extends JpaRepository<Experiment, String> {
    List<Experiment> findByStudyId(String studyId);

    void deleteAllByStudyId(String studyId);

    void deleteByStudyIdAndExperimentIdNotIn(String studyId, Collection<String> experimentIds);

}
