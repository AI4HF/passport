package io.passport.server.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.passport.server.model.Dataset;
import io.passport.server.model.Personnel;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.DatasetRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
/**
 * Service class for Dataset management.
 */
@Service
public class DatasetService {

    /**
     * Dataset and Personnel repo access for database management.
     */
    private final DatasetRepository datasetRepository;
    private final PersonnelService personnelService;
    private final RoleCheckerService roleCheckerService;

    /**
     * Lazy service references for limited use in cascade validation
     */
    @Autowired @Lazy private LearningDatasetService learningDatasetService;
    @Autowired @Lazy private FeatureDatasetCharacteristicService featureDatasetCharacteristicService;

    @Autowired
    public DatasetService(DatasetRepository datasetRepository, PersonnelService personnelService, RoleCheckerService roleCheckerService) {
        this.datasetRepository = datasetRepository;
        this.personnelService = personnelService;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Starts a validation chain of Datasets and all of their children for cascades
     *
     * @param studyId Id of the Study
     * @param datasetId Id of the Dataset
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateDatasetDeletion(String studyId, String datasetId, Jwt principal) {
        List<ValidationResult> results = new ArrayList<>();

        results.add(learningDatasetService.validateCascade(studyId, "Dataset", datasetId, principal));
        results.add(featureDatasetCharacteristicService.validateCascade(studyId, "Dataset", datasetId, principal));

        return ValidationResult.aggregate(results);
    }

    /**
     * Determines which entities are to be cascaded based on the request from the previous element in the chain
     * Continues the chain by directing to the next entries through the other validation method
     *
     * @param studyId Id of the Study
     * @param sourceResourceType Resource type of the parent element in the Cascade chain
     * @param sourceResourceId Resource id of the parent element in the Cascade chain
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateCascade(String studyId, String sourceResourceType, String sourceResourceId, Jwt principal) {
        List<Dataset> affectedDatasets;
        switch (sourceResourceType) {
            case "FeatureSet":
                affectedDatasets = datasetRepository.findByFeaturesetId(sourceResourceId);
                break;
            case "Population":
                affectedDatasets = datasetRepository.findByPopulationId(sourceResourceId);
                break;
            case "Organization":
                affectedDatasets = datasetRepository.findByOrganizationId(sourceResourceId);
                break;
            default:
                return new ValidationResult(true, "");
        }

        if (affectedDatasets.isEmpty()) {
            return new ValidationResult(true, "");
        }

        List<ValidationResult> childResults = new ArrayList<>();
        boolean authorized = true;

        for (Dataset ds : affectedDatasets) {
            boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                    studyId,
                    principal,
                    List.of(Role.DATA_ENGINEER, Role.DATA_SCIENTIST)
            );

            if (!hasPermission) {
                authorized = false;
                break;
            }
            childResults.add(validateDatasetDeletion(studyId, ds.getDatasetId(), principal));
        }

        if (!authorized) {
            return new ValidationResult(false, "Dataset");
        }

        childResults.add(new ValidationResult(true, "Dataset"));

        return ValidationResult.aggregate(childResults);
    }

    /**
     * Return all Datasets
     * @return
     */
    public List<Dataset> getAllDatasets() {
        return datasetRepository.findAll();
    }

    /** Return all Datasets for a study, but with FK names instead of IDs */
    public List<Dataset> getAllDatasetsWithNamesByStudyId(String studyId) {
        return datasetRepository.findDatasetWithNamesByStudyId(studyId);
    }


    /**
     * Return all Datasets by studyId
     * @param studyId ID of the study
     * @return
     */
    public List<Dataset> getAllDatasetsByStudyId(String studyId) {
        return datasetRepository.findDatasetByStudyId(studyId);
    }

    /**
     * Find a Dataset by datasetId
     * @param datasetId ID of the Dataset
     * @return
     */
    public Optional<Dataset> findDatasetByDatasetId(String datasetId) {
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
    public Optional<Dataset> updateDataset(String datasetId, Dataset updatedDataset, String personnelId) {
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
    public Optional<Dataset> deleteDataset(String datasetId) {
        Optional<Dataset> existingDataset = datasetRepository.findById(datasetId);
        if (existingDataset.isPresent()) {
            datasetRepository.delete(existingDataset.get());
            return existingDataset;
        } else {
            return Optional.empty();
        }
    }

    /**
     * Find Datasets created or last updated by a specific Personnel
     * @param personnelId Id of the Personnel
     */
    public List<Dataset> findByCreatedByOrLastUpdatedBy(String personnelId) {
        return datasetRepository.findByCreatedByOrLastUpdatedBy(personnelId);
    }

    /**
     * Resolve the Study ID for a given Dataset ID directly via a repository call
     * @param datasetId Id of the Dataset
     */
    public Optional<String> findStudyIdByDatasetId(String datasetId) {
        return datasetRepository.findStudyIdByDatasetId(datasetId);
    }
}

