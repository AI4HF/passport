package io.passport.server.repository;

import io.passport.server.model.FeatureSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * FeatureSet repository for database management.
 */
@Repository
public interface FeatureSetRepository extends JpaRepository<FeatureSet, Long> {

}
