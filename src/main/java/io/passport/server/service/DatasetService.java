package io.passport.server.service;

import io.passport.server.model.Dataset;
import io.passport.server.model.Personnel;
import io.passport.server.model.Population;
import io.passport.server.repository.DatasetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
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

    /**
     * Population service for population management.
     */
    private final PopulationService populationService;

    /**
     * Personnel service for personnel management.
     */
    private final PersonnelService personnelService;

    @Autowired
    public DatasetService(DatasetRepository datasetRepository, PopulationService populationService, PersonnelService personnelService) {
        this.datasetRepository = datasetRepository;
        this.populationService = populationService;
        this.personnelService = personnelService;
    }

    /**
     * Return all Datasets
     * @return
     */
    public List<Dataset> getAllDatasets() {
        return datasetRepository.findAll();
    }

    /**
     * Return all Datasets by studyId
     * @param studyId ID of the study
     * @return
     */
    public List<Dataset> getAllDatasetsByStudyId(Long studyId) {
        return datasetRepository.findDatasetByStudyId(studyId);
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
     * @param personnelId ID of the personnel
     * @return
     */
    public Optional<Dataset> saveDataset(Dataset dataset, String personnelId) {
        Optional<Personnel> personnel = this.personnelService.findPersonnelById(personnelId);
        if(personnel.isPresent()) {
            dataset.setCreatedAt(Instant.now());
            dataset.setLastUpdatedAt(Instant.now());
            dataset.setPopulationId(dataset.getPopulationId());
            dataset.setOrganizationId(personnel.get().getOrganizationId());
            return Optional.of(datasetRepository.save(dataset));
        }else{
            return Optional.empty();
        }
    }

    /**
     * Update a Dataset
     * @param datasetId ID of the Dataset
     * @param updatedDataset Dataset to be updated
     * @param personnelId ID of the personnel
     * @return
     */
    public Optional<Dataset> updateDataset(Long datasetId, Dataset updatedDataset, String personnelId) {
        Optional<Dataset> oldDataset = datasetRepository.findById(datasetId);
        Optional<Personnel> personnel = this.personnelService.findPersonnelById(personnelId);
        if (oldDataset.isPresent() && personnel.isPresent()) {
            Dataset dataset = oldDataset.get();
            dataset.setFeaturesetId(updatedDataset.getFeaturesetId());
            dataset.setPopulationId(updatedDataset.getPopulationId());
            dataset.setOrganizationId(personnel.get().getOrganizationId());
            dataset.setTitle(updatedDataset.getTitle());
            dataset.setDescription(updatedDataset.getDescription());
            dataset.setVersion(updatedDataset.getVersion());
            dataset.setReferenceEntity(updatedDataset.getReferenceEntity());
            dataset.setNumOfRecords(updatedDataset.getNumOfRecords());
            dataset.setSynthetic(updatedDataset.getSynthetic());
            dataset.setLastUpdatedAt(Instant.now());
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
    public Optional<Dataset> deleteDataset(Long datasetId) {
        Optional<Dataset> existingDataset = datasetRepository.findById(datasetId);
        if (existingDataset.isPresent()) {
            datasetRepository.delete(existingDataset.get());
            return existingDataset;
        } else {
            return Optional.empty();
        }
    }
}

