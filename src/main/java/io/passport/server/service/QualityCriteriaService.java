package io.passport.server.service;

import io.passport.server.model.QualityCriteria;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.QualityCriteriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for QualityCriteria management.
 */
@Service
public class QualityCriteriaService {

    /**
     * QualityCriteria repo access for database management.
     */
    private final QualityCriteriaRepository qualityCriteriaRepository;
    private final RoleCheckerService roleCheckerService;

    /**
     * Lazy service references for limited use in cascade validation
     */
    @Autowired @Lazy private QualityCriterionService qualityCriterionService;
    @Autowired @Lazy private QualityAssessmentService qualityAssessmentService;

    @Autowired
    public QualityCriteriaService(QualityCriteriaRepository qualityCriteriaRepository,
                                  RoleCheckerService roleCheckerService) {
        this.qualityCriteriaRepository = qualityCriteriaRepository;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Starts a validation chain of QualityCriteria and all of their children for cascades
     *
     * @param studyId Id of the Study
     * @param qualityCriteriaId Id of the QualityCriteria
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateQualityCriteriaDeletion(String studyId, String qualityCriteriaId, Jwt principal) {
        List<ValidationResult> results = new ArrayList<>();

        results.add(qualityCriterionService.validateCascade(studyId, "QualityCriteria", qualityCriteriaId, principal));
        results.add(qualityAssessmentService.validateCascade(studyId, "QualityCriteria", qualityCriteriaId, principal));

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
        List<QualityCriteria> affectedQualityCriteria;

        switch (sourceResourceType) {
            case "Experiment":
                affectedQualityCriteria = qualityCriteriaRepository.findByExperimentId(sourceResourceId);
                break;
            default:
                return new ValidationResult(true, "");
        }

        if (affectedQualityCriteria.isEmpty()) {
            return new ValidationResult(true, "");
        }

        List<ValidationResult> childResults = new ArrayList<>();
        boolean authorized = true;

        for (QualityCriteria qc : affectedQualityCriteria) {
            boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                    studyId,
                    principal,
                    List.of(Role.DATA_ENGINEER)
            );

            if (!hasPermission) {
                authorized = false;
                break;
            }

            childResults.add(validateQualityCriteriaDeletion(studyId, qc.getQualityCriteriaId(), principal));
        }

        if (!authorized) {
            return new ValidationResult(false, "QualityCriteria");
        }

        childResults.add(new ValidationResult(true, "QualityCriteria"));

        return ValidationResult.aggregate(childResults);
    }

    /**
     * Return all QualityCriteria of a study
     * @param studyId ID of the study
     * @return
     */
    public List<QualityCriteria> getAllQualityCriteriaByStudyId(String studyId) {
        return qualityCriteriaRepository.findQualityCriteriaByStudyId(studyId);
    }

    /**
     * Return all QualityCriteria of an experiment
     * @param experimentId ID of the experiment
     * @return
     */
    public List<QualityCriteria> findQualityCriteriaByExperimentId(String experimentId) {
        return qualityCriteriaRepository.findByExperimentId(experimentId);
    }

    /**
     * Find a QualityCriteria by qualityCriteriaId
     * @param qualityCriteriaId ID of the QualityCriteria
     * @return
     */
    public Optional<QualityCriteria> findQualityCriteriaById(String qualityCriteriaId) {
        return qualityCriteriaRepository.findById(qualityCriteriaId);
    }

    /**
     * Find the study a QualityCriteria belongs to
     * @param qualityCriteriaId ID of the QualityCriteria
     * @return
     */
    public String findStudyIdByQualityCriteriaId(String qualityCriteriaId) {
        return qualityCriteriaRepository.findStudyIdByQualityCriteriaId(qualityCriteriaId);
    }

    /**
     * Save a QualityCriteria
     * @param qualityCriteria QualityCriteria to be saved
     * @return
     */
    public QualityCriteria saveQualityCriteria(QualityCriteria qualityCriteria) {
        qualityCriteria.setCreatedAt(Instant.now());
        qualityCriteria.setLastUpdatedAt(Instant.now());
        return qualityCriteriaRepository.save(qualityCriteria);
    }

    /**
     * Update a QualityCriteria
     * @param qualityCriteriaId ID of the QualityCriteria
     * @param updatedQualityCriteria QualityCriteria to be updated
     * @return
     */
    public Optional<QualityCriteria> updateQualityCriteria(String qualityCriteriaId, QualityCriteria updatedQualityCriteria) {
        Optional<QualityCriteria> oldQualityCriteria = qualityCriteriaRepository.findById(qualityCriteriaId);
        if (oldQualityCriteria.isPresent()) {
            QualityCriteria qualityCriteria = oldQualityCriteria.get();
            qualityCriteria.setExperimentId(updatedQualityCriteria.getExperimentId());
            qualityCriteria.setTitle(updatedQualityCriteria.getTitle());
            qualityCriteria.setUrl(updatedQualityCriteria.getUrl());
            qualityCriteria.setDescription(updatedQualityCriteria.getDescription());
            qualityCriteria.setVersion(updatedQualityCriteria.getVersion());
            qualityCriteria.setLastUpdatedBy(updatedQualityCriteria.getLastUpdatedBy());
            qualityCriteria.setLastUpdatedAt(Instant.now());
            QualityCriteria savedQualityCriteria = qualityCriteriaRepository.save(qualityCriteria);
            return Optional.of(savedQualityCriteria);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a QualityCriteria
     * @param qualityCriteriaId ID of QualityCriteria to be deleted
     * @return
     */
    public boolean deleteQualityCriteria(String qualityCriteriaId) {
        if (qualityCriteriaRepository.existsById(qualityCriteriaId)) {
            qualityCriteriaRepository.deleteById(qualityCriteriaId);
            return true;
        } else {
            return false;
        }
    }
}
