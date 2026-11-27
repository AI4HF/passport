package io.passport.server.repository;

import io.passport.server.model.Dataset;
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
public interface DatasetRepository extends JpaRepository<Dataset, String> {

    List<Dataset> findByFeaturesetId(String featuresetId);
    List<Dataset> findByPopulationId(String populationId);
    List<Dataset> findByOrganizationId(String organizationId);

    // Join with population table and get related Dataset for the study (whole entities, so that the v2.7 fields
    // such as previousDatasetId, wasGeneratedBy and the HealthDCAT-AP coverage fields are not dropped)
    @Query("SELECT d FROM Dataset d, Population p WHERE d.populationId = p.populationId AND p.studyId = :studyId")
    List<Dataset> findDatasetByStudyId(@Param("studyId") String studyId);

    // return the names of the related entities instead of their IDs, with every other field of the dataset
    // (the Lombok all-args constructor, so the arguments follow the field order of Dataset; a field added to
    // Dataset must be added here too, or the query fails at startup)
    @Query("""
       SELECT new Dataset(
           d.datasetId,
           fs.title,
           p.populationUrl,
           o.name,
           d.title,
           d.description,
           d.version,
           d.referenceEntity,
           d.numberOfRecords,
           d.synthetic,
           d.previousDatasetId,
           d.persistentIdentifier,
           d.structuredData,
           d.temporalCoverageStart,
           d.temporalCoverageEnd,
           d.temporalResolution,
           d.geographicalCoverage,
           d.numberOfUniqueIndividuals,
           d.minTypicalAge,
           d.maxTypicalAge,
           d.conformsTo,
           d.provenanceStatement,
           d.wasGeneratedBy,
           d.createdAt,
           d.createdBy,
           d.lastUpdatedAt,
           d.lastUpdatedBy)
       FROM Dataset d
       JOIN FeatureSet   fs ON d.featuresetId   = fs.featuresetId
       JOIN Population   p  ON d.populationId   = p.populationId
       JOIN Organization o  ON d.organizationId = o.organizationId
       WHERE p.studyId = :studyId
       """)
    List<Dataset> findDatasetWithNamesByStudyId(@Param("studyId") String studyId);

    // Find Datasets modified by a specific personnel
    @Query("SELECT d FROM Dataset d WHERE d.createdBy = :personnelId OR d.lastUpdatedBy = :personnelId")
    List<Dataset> findByCreatedByOrLastUpdatedBy(@Param("personnelId") String personnelId);

    // Find Study ID directly from Dataset ID
    @Query("SELECT p.studyId FROM Dataset d JOIN Population p ON d.populationId = p.populationId WHERE d.datasetId = :datasetId")
    Optional<String> findStudyIdByDatasetId(@Param("datasetId") String datasetId);


}
