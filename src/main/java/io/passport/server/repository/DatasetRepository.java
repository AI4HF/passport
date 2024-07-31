package io.passport.server.repository;

import io.passport.server.model.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Dataset repository for database management.
 */
@Repository
public interface DatasetRepository extends JpaRepository<Dataset, Long> {

}
