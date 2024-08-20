package io.passport.server.repository;

import io.passport.server.model.Dataset;
import io.passport.server.model.FeatureSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Dataset repository for database management.
 */
@Repository
public interface DatasetRepository extends JpaRepository<Dataset, Long> {
    // Join with studyPersonnel table and get related Dataset for the personnel
    @Query("SELECT new Dataset(d.datasetId, d.featuresetId, d.populationId, d.organizationId, d.title, d.description, d.version, d.referenceEntity, d.numOfRecords, d.synthetic, d.createdAt, d.createdBy, d.lastUpdatedAt, d.lastUpdatedBy)  " +
            "FROM Dataset d, StudyPersonnel sp, Population p WHERE d.populationId = p.populationId AND p.studyId = sp.id.studyId AND sp.id.personnelId = :personnelId")
    List<Dataset> findDatasetByPersonnelId(@Param("personnelId") String personnelId);

    // Join with studyPersonnel table and get related Dataset for the personnel
    @Query("SELECT new Dataset(d.datasetId, d.featuresetId, d.populationId, d.organizationId, d.title, d.description, d.version, d.referenceEntity, d.numOfRecords, d.synthetic, d.createdAt, d.createdBy, d.lastUpdatedAt, d.lastUpdatedBy)  " +
            "FROM Dataset d, StudyPersonnel sp, Population p WHERE d.populationId = p.populationId AND p.studyId = sp.id.studyId AND sp.id.personnelId = :personnelId AND d.datasetId = :datasetId")
    Optional<Dataset> findByIdAndPersonnelId(@Param("datasetId") Long datasetId, @Param("personnelId") String personnelId);
}
