package io.passport.server.repository;

import io.passport.server.model.Experiment;
import io.passport.server.model.FeatureSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Experiment repository for database management.
 */
public interface ExperimentRepository extends JpaRepository<Experiment, String> {
    List<Experiment> findByStudyId(String studyId);
    void deleteAllByStudyId(String studyId);

    // Join with studyPersonnel table and get related experiments for the personnel
    @Query("SELECT new Experiment(e.experimentId, e.studyId, e.researchQuestion)  " +
            "FROM StudyPersonnel sp, Experiment e WHERE sp.id.studyId = e.studyId AND sp.id.personnelId = :personnelId")
    List<Experiment> findExperimentsByPersonnelId(@Param("personnelId") String personnelId);
}
