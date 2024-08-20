package io.passport.server.repository;

import io.passport.server.model.FeatureSet;
import io.passport.server.model.Study;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * FeatureSet repository for database management.
 */
@Repository
public interface FeatureSetRepository extends JpaRepository<FeatureSet, Long> {

    // Join with studyPersonnel table and get related featureSets for the personnel
    @Query("SELECT new FeatureSet(fs.featuresetId, fs.experimentId, fs.title, fs.featuresetURL, fs.description, fs.createdAt, fs.createdBy, fs.lastUpdatedAt, fs.lastUpdatedBy)  " +
            "FROM FeatureSet fs, StudyPersonnel sp, Experiment e WHERE fs.experimentId = e.experimentId AND sp.id.studyId = e.studyId AND sp.id.personnelId = :personnelId")
    List<FeatureSet> findFeatureSetByPersonnelId(@Param("personnelId") String personnelId);

    // Join with studyPersonnel table and get related featureSet for the personnel
    @Query("SELECT new FeatureSet(fs.featuresetId, fs.experimentId, fs.title, fs.featuresetURL, fs.description, fs.createdAt, fs.createdBy, fs.lastUpdatedAt, fs.lastUpdatedBy)  " +
            "FROM FeatureSet fs, StudyPersonnel sp, Experiment e WHERE fs.experimentId = e.experimentId AND sp.id.studyId = e.studyId AND sp.id.personnelId = :personnelId AND fs.featuresetId = :featuresetId")
    Optional<FeatureSet> findByIdAndPersonnelId(@Param("featuresetId") Long featuresetId, @Param("personnelId") String personnelId);

}
