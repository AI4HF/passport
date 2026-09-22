package io.passport.server.repository;

import io.passport.server.model.CatalogueRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CatalogueRegistration repository for database management.
 */
@Repository
public interface CatalogueRegistrationRepository extends JpaRepository<CatalogueRegistration, String> {

    List<CatalogueRegistration> findByCatalogueDatasetId(String catalogueDatasetId);

    List<CatalogueRegistration> findByCatalogueDatasetIdAndCatalogueType(String catalogueDatasetId, String catalogueType);
}
