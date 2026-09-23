package io.passport.server.repository;

import io.passport.server.model.CatalogueDataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CatalogueDataset repository for database management.
 */
@Repository
public interface CatalogueDatasetRepository extends JpaRepository<CatalogueDataset, String> {

    Optional<CatalogueDataset> findByDatasetId(String datasetId);

    // The publication records of a study, reached through the dataset's population
    @Query("SELECT cd FROM CatalogueDataset cd, Dataset d, Population p " +
            "WHERE cd.datasetId = d.datasetId AND d.populationId = p.populationId AND p.studyId = :studyId")
    List<CatalogueDataset> findCatalogueDatasetsByStudyId(@Param("studyId") String studyId);
}
