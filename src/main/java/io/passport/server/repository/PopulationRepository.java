package io.passport.server.repository;

import io.passport.server.model.Population;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Population repository for database management.
 */
@Repository
public interface PopulationRepository extends JpaRepository<Population, String> {
    List<Population> findByStudyId(String studyId);

    // Join with Experiment table and get related population
    @Query("SELECT new Population(p.populationId, p.studyId, p.populationUrl, p.description, p.characteristics)  " +
            "FROM FeatureSet fs, Population p, Experiment e WHERE fs.experimentId = e.experimentId AND p.studyId = e.studyId AND fs.featuresetId = :featuresetId")
    List<Population> findByFeatureSetId(@Param("featuresetId") String featuresetId);
}
