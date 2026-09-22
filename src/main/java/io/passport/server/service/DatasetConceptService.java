package io.passport.server.service;

import io.passport.server.model.DatasetConcept;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.DatasetConceptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for DatasetConcept management.
 */
@Service
public class DatasetConceptService {

    /**
     * DatasetConcept repo access for database management.
     */
    private final DatasetConceptRepository datasetConceptRepository;
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public DatasetConceptService(DatasetConceptRepository datasetConceptRepository, RoleCheckerService roleCheckerService) {
        this.datasetConceptRepository = datasetConceptRepository;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Determines which entities are to be cascaded based on the request from the previous element
     * in the chain.
     *
     * @param studyId Id of the Study
     * @param sourceResourceType Resource type of the parent element in the Cascade chain
     * @param sourceResourceId Resource id of the parent element in the Cascade chain
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateCascade(String studyId, String sourceResourceType, String sourceResourceId, Jwt principal) {
        List<DatasetConcept> affected;

        switch (sourceResourceType) {
            case "Dataset":
                affected = datasetConceptRepository.findByDatasetId(sourceResourceId);
                break;
            default:
                return new ValidationResult(true, "");
        }

        if (affected.isEmpty()) {
            return new ValidationResult(true, "");
        }

        boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                studyId,
                principal,
                List.of(Role.DATA_ENGINEER, Role.DATA_STEWARD)
        );

        if (!hasPermission) {
            return new ValidationResult(false, "DatasetConcept");
        }

        return new ValidationResult(true, "DatasetConcept");
    }

    /**
     * Return the controlled-vocabulary values of a dataset
     * @param datasetId ID of the dataset
     * @return
     */
    public List<DatasetConcept> findByDatasetId(String datasetId) {
        return datasetConceptRepository.findByDatasetId(datasetId);
    }

    /**
     * Find a DatasetConcept by conceptId
     * @param conceptId ID of the DatasetConcept
     * @return
     */
    public Optional<DatasetConcept> findDatasetConceptById(String conceptId) {
        return datasetConceptRepository.findById(conceptId);
    }

    /**
     * Save a DatasetConcept
     * @param datasetConcept DatasetConcept to be saved
     * @return
     */
    public DatasetConcept saveDatasetConcept(DatasetConcept datasetConcept) {
        return datasetConceptRepository.save(datasetConcept);
    }

    /**
     * Update a DatasetConcept
     * @param conceptId ID of the DatasetConcept
     * @param updated DatasetConcept to be updated
     * @return
     */
    public Optional<DatasetConcept> updateDatasetConcept(String conceptId, DatasetConcept updated) {
        Optional<DatasetConcept> existing = datasetConceptRepository.findById(conceptId);
        if (existing.isPresent()) {
            DatasetConcept datasetConcept = existing.get();
            datasetConcept.setDatasetId(updated.getDatasetId());
            datasetConcept.setPropertyUri(updated.getPropertyUri());
            datasetConcept.setConceptUri(updated.getConceptUri());
            datasetConcept.setPrefLabel(updated.getPrefLabel());
            datasetConcept.setConceptScheme(updated.getConceptScheme());
            return Optional.of(datasetConceptRepository.save(datasetConcept));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a DatasetConcept
     * @param conceptId ID of the DatasetConcept to be deleted
     * @return
     */
    public boolean deleteDatasetConcept(String conceptId) {
        if (datasetConceptRepository.existsById(conceptId)) {
            datasetConceptRepository.deleteById(conceptId);
            return true;
        } else {
            return false;
        }
    }
}
