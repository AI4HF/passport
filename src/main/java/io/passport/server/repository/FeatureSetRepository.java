package io.passport.server.repository;

import io.passport.server.model.FeatureSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * FeatureSet repository for database management.
 */
@Repository
public interface FeatureSetRepository extends JpaRepository<FeatureSet, String> {

    // Join with experiment table and get related featureSets for the study
    @Query("SELECT new FeatureSet(fs.featuresetId, fs.experimentId, fs.title, fs.featuresetURL, fs.description, fs.createdAt, fs.createdBy, fs.lastUpdatedAt, fs.lastUpdatedBy)  " +
            "FROM FeatureSet fs, Experiment e WHERE fs.experimentId = e.experimentId AND e.studyId = :studyId")
    List<FeatureSet> findFeatureSetByStudyId(@Param("studyId") String studyId);

}
