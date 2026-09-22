package io.passport.server.repository;

import io.passport.server.model.DatasetDistribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * DatasetDistribution repository for database management.
 */
@Repository
public interface DatasetDistributionRepository extends JpaRepository<DatasetDistribution, String> {

    List<DatasetDistribution> findByCatalogueDatasetId(String catalogueDatasetId);
}
