package io.passport.server.repository;

import io.passport.server.model.DatasetConcept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * DatasetConcept repository for database management.
 */
@Repository
public interface DatasetConceptRepository extends JpaRepository<DatasetConcept, String> {

    List<DatasetConcept> findByDatasetId(String datasetId);
}
