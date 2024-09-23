package io.passport.server.repository;

import io.passport.server.model.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Dataset repository for database management.
 */
@Repository
public interface DatasetRepository extends JpaRepository<Dataset, Long> {

    // Join with population table and get related Dataset for the study
    @Query("SELECT new Dataset(d.datasetId, d.featuresetId, d.populationId, d.organizationId, d.title, d.description, d.version, d.referenceEntity, d.numOfRecords, d.synthetic, d.createdAt, d.createdBy, d.lastUpdatedAt, d.lastUpdatedBy)  " +
            "FROM Dataset d, Population p WHERE d.populationId = p.populationId AND p.studyId = :studyId")
    List<Dataset> findDatasetByStudyId(@Param("studyId") Long studyId);

}
