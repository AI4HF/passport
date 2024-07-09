package io.passport.server.repository;

import io.passport.server.model.FeatureDatasetCharacteristic;
import io.passport.server.model.FeatureDatasetCharacteristicId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * FeatureDatasetCharacteristic repository for database management.
 */
@Repository
public interface FeatureDatasetCharacteristicRepository extends JpaRepository<FeatureDatasetCharacteristic, FeatureDatasetCharacteristicId> {
    List<FeatureDatasetCharacteristic> findByIdDatasetId(Long datasetId);
    List<FeatureDatasetCharacteristic> findByIdFeatureId(Long featureId);
}
