package io.passport.server.service;

import io.passport.server.model.Dataset;
import io.passport.server.repository.DatasetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for Dataset management.
 */
@Service
public class DatasetService {

    /**
     * Dataset repo access for database management.
     */
    private final DatasetRepository datasetRepository;

    @Autowired
    public DatasetService(DatasetRepository datasetRepository) {
        this.datasetRepository = datasetRepository;
    }

    /**
     * Return all Datasets
     * @return
     */
    public List<Dataset> getAllDatasets() {
        return datasetRepository.findAll();
    }

    /**
     * Find a Dataset by datasetId
     * @param datasetId ID of the Dataset
     * @return
     */
    public Optional<Dataset> findDatasetByDatasetId(Long datasetId) {
        return datasetRepository.findById(datasetId);
    }

    /**
     * Save a Dataset
     * @param dataset Dataset to be saved
     * @return
     */
    public Dataset saveDataset(Dataset dataset) {
        return datasetRepository.save(dataset);
    }

    /**
     * Update a Dataset
     * @param datasetId ID of the Dataset
     * @param updatedDataset Dataset to be updated
     * @return
     */
    public Optional<Dataset> updateDataset(Long datasetId, Dataset updatedDataset) {
        Optional<Dataset> oldDataset = datasetRepository.findById(datasetId);
        if (oldDataset.isPresent()) {
            Dataset dataset = oldDataset.get();
            dataset.setFeaturesetId(updatedDataset.getFeaturesetId());
            dataset.setPopulationId(updatedDataset.getPopulationId());
            dataset.setOrganizationId(updatedDataset.getOrganizationId());
            dataset.setTitle(updatedDataset.getTitle());
            dataset.setDescription(updatedDataset.getDescription());
            dataset.setVersion(updatedDataset.getVersion());
            dataset.setReferenceEntity(updatedDataset.getReferenceEntity());
            dataset.setNumOfRecords(updatedDataset.getNumOfRecords());
            dataset.setSynthetic(updatedDataset.getSynthetic());
            dataset.setCreatedAt(updatedDataset.getCreatedAt());
            dataset.setCreatedBy(updatedDataset.getCreatedBy());
            dataset.setLastUpdatedAt(updatedDataset.getLastUpdatedAt());
            dataset.setLastUpdatedBy(updatedDataset.getLastUpdatedBy());
            Dataset savedDataset = datasetRepository.save(dataset);
            return Optional.of(savedDataset);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a Dataset
     * @param datasetId ID of Dataset to be deleted
     * @return
     */
    public boolean deleteDataset(Long datasetId) {
        if(datasetRepository.existsById(datasetId)) {
            datasetRepository.deleteById(datasetId);
            return true;
        } else {
            return false;
        }
    }
}

