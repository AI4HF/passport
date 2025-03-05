package io.passport.server.service;

import io.passport.server.model.FeatureDatasetCharacteristic;
import io.passport.server.model.FeatureDatasetCharacteristicId;
import io.passport.server.repository.FeatureDatasetCharacteristicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for FeatureDatasetCharacteristic management.
 */
@Service
public class FeatureDatasetCharacteristicService {

    /**
     * FeatureDatasetCharacteristic repo access for database management.
     */
    private final FeatureDatasetCharacteristicRepository featureDatasetCharacteristicRepository;

    @Autowired
    public FeatureDatasetCharacteristicService(FeatureDatasetCharacteristicRepository featureDatasetCharacteristicRepository) {
        this.featureDatasetCharacteristicRepository = featureDatasetCharacteristicRepository;
    }

    /**
     * Return all FeatureDatasetCharacteristics
     * @return
     */
    public List<FeatureDatasetCharacteristic> getAllFeatureDatasetCharacteristics() {
        return featureDatasetCharacteristicRepository.findAll();
    }

    /**
     * Find FeatureDatasetCharacteristics by datasetId
     * @param datasetId ID of the Dataset
     * @return
     */
    public List<FeatureDatasetCharacteristic> findByDatasetId(Long datasetId) {
        return featureDatasetCharacteristicRepository.findByIdDatasetId(datasetId);
    }

    /**
     * Find FeatureDatasetCharacteristics by featureId
     * @param featureId ID of the Feature
     * @return
     */
    public List<FeatureDatasetCharacteristic> findByFeatureId(Long featureId) {
        return featureDatasetCharacteristicRepository.findByIdFeatureId(featureId);
    }

    /**
     * Find a FeatureDatasetCharacteristic by composite id
     * @param featureDatasetCharacteristicId composite ID of the FeatureDatasetCharacteristic
     * @return
     */
    public Optional<FeatureDatasetCharacteristic> findFeatureDatasetCharacteristicById(FeatureDatasetCharacteristicId featureDatasetCharacteristicId) {
        return featureDatasetCharacteristicRepository.findById(featureDatasetCharacteristicId);
    }

    /**
     * Save a FeatureDatasetCharacteristic
     * @param featureDatasetCharacteristic FeatureDatasetCharacteristic to be saved
     * @return
     */
    public FeatureDatasetCharacteristic saveFeatureDatasetCharacteristic(FeatureDatasetCharacteristic featureDatasetCharacteristic) {
        return featureDatasetCharacteristicRepository.save(featureDatasetCharacteristic);
    }

    /**
     * Update a FeatureDatasetCharacteristic
     * @param featureDatasetCharacteristicId composite ID of the FeatureDatasetCharacteristic
     * @param updatedFeatureDatasetCharacteristic FeatureDatasetCharacteristic to be updated
     * @return
     */
    public Optional<FeatureDatasetCharacteristic> updateFeatureDatasetCharacteristic(FeatureDatasetCharacteristicId featureDatasetCharacteristicId, FeatureDatasetCharacteristic updatedFeatureDatasetCharacteristic) {
        Optional<FeatureDatasetCharacteristic> oldFeatureDatasetCharacteristic = featureDatasetCharacteristicRepository.findById(featureDatasetCharacteristicId);
        if (oldFeatureDatasetCharacteristic.isPresent()) {
            FeatureDatasetCharacteristic featureDatasetCharacteristic = oldFeatureDatasetCharacteristic.get();
            featureDatasetCharacteristic.setCharacteristicName(updatedFeatureDatasetCharacteristic.getCharacteristicName());
            featureDatasetCharacteristic.setValue(updatedFeatureDatasetCharacteristic.getValue());
            featureDatasetCharacteristic.setValueDataType(updatedFeatureDatasetCharacteristic.getValueDataType());
            FeatureDatasetCharacteristic savedFeatureDatasetCharacteristic = featureDatasetCharacteristicRepository.save(featureDatasetCharacteristic);
            return Optional.of(savedFeatureDatasetCharacteristic);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a FeatureDatasetCharacteristic
     * @param featureDatasetCharacteristicId composite ID of FeatureDatasetCharacteristic to be deleted
     * @return
     */
    public Optional<FeatureDatasetCharacteristic> deleteFeatureDatasetCharacteristic(FeatureDatasetCharacteristicId featureDatasetCharacteristicId) {
        Optional<FeatureDatasetCharacteristic> existingCharacteristic = featureDatasetCharacteristicRepository.findById(featureDatasetCharacteristicId);
        if (existingCharacteristic.isPresent()) {
            featureDatasetCharacteristicRepository.delete(existingCharacteristic.get());
            return existingCharacteristic;
        } else {
            return Optional.empty();
        }
    }

}
