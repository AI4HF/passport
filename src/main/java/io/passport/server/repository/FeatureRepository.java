package io.passport.server.repository;

import io.passport.server.model.Feature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Feature repository for database management.
 */
@Repository
public interface FeatureRepository extends JpaRepository<Feature, String> {
    List<Feature> findByFeaturesetId(String featuresetId);
}
