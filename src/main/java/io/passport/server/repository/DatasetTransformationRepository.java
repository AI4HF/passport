package io.passport.server.repository;

import io.passport.server.model.DatasetTransformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * DatasetTransformation repository for database management.
 */
@Repository
public interface DatasetTransformationRepository extends JpaRepository<DatasetTransformation, String> {

}
