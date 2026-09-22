package io.passport.server.service;

import io.passport.server.model.QualityAssessment;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.QualityAssessmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for QualityAssessment management.
 */
@Service
public class QualityAssessmentService {

    /**
     * QualityAssessment repo access for database management.
     */
    private final QualityAssessmentRepository qualityAssessmentRepository;
    private final RoleCheckerService roleCheckerService;

    /**
     * Lazy service references for limited use in cascade validation
     */
    @Autowired @Lazy private QualityCriterionAssessmentResultService qualityCriterionAssessmentResultService;

    @Autowired
    public QualityAssessmentService(QualityAssessmentRepository qualityAssessmentRepository,
                                    RoleCheckerService roleCheckerService) {
        this.qualityAssessmentRepository = qualityAssessmentRepository;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Starts a validation chain of QualityAssessment and all of their children for cascades
     *
     * @param studyId Id of the Study
     * @param qualityAssessmentId Id of the QualityAssessment
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateQualityAssessmentDeletion(String studyId, String qualityAssessmentId, Jwt principal) {
        List<ValidationResult> results = new ArrayList<>();

        results.add(qualityCriterionAssessmentResultService.validateCascade(
                studyId, "QualityAssessment", qualityAssessmentId, principal));

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
        List<QualityAssessment> affectedAssessments;

        switch (sourceResourceType) {
            case "Dataset":
                affectedAssessments = qualityAssessmentRepository.findByDatasetId(sourceResourceId);
                break;
            case "QualityCriteria":
                affectedAssessments = qualityAssessmentRepository.findByQualityCriteriaId(sourceResourceId);
                break;
            default:
                return new ValidationResult(true, "");
        }

        if (affectedAssessments.isEmpty()) {
            return new ValidationResult(true, "");
        }

        List<ValidationResult> childResults = new ArrayList<>();
        boolean authorized = true;

        for (QualityAssessment assessment : affectedAssessments) {
            boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                    studyId,
                    principal,
                    List.of(Role.DATA_ENGINEER)
            );

            if (!hasPermission) {
                authorized = false;
                break;
            }

            childResults.add(validateQualityAssessmentDeletion(studyId, assessment.getQualityAssessmentId(), principal));
        }

        if (!authorized) {
            return new ValidationResult(false, "QualityAssessment");
        }

        childResults.add(new ValidationResult(true, "QualityAssessment"));

        return ValidationResult.aggregate(childResults);
    }

    /**
     * Return all QualityAssessments of a study
     * @param studyId ID of the study
     * @return
     */
    public List<QualityAssessment> getAllQualityAssessmentsByStudyId(String studyId) {
        return qualityAssessmentRepository.findQualityAssessmentsByStudyId(studyId);
    }

    /**
     * Return all QualityAssessments of a dataset, i.e. its quality history
     * @param datasetId ID of the dataset
     * @return
     */
    public List<QualityAssessment> findQualityAssessmentsByDatasetId(String datasetId) {
        return qualityAssessmentRepository.findByDatasetId(datasetId);
    }

    /**
     * Find a QualityAssessment by qualityAssessmentId
     * @param qualityAssessmentId ID of the QualityAssessment
     * @return
     */
    public Optional<QualityAssessment> findQualityAssessmentById(String qualityAssessmentId) {
        return qualityAssessmentRepository.findById(qualityAssessmentId);
    }

    /**
     * Find the study a QualityAssessment belongs to
     * @param qualityAssessmentId ID of the QualityAssessment
     * @return
     */
    public String findStudyIdByQualityAssessmentId(String qualityAssessmentId) {
        return qualityAssessmentRepository.findStudyIdByQualityAssessmentId(qualityAssessmentId);
    }

    /**
     * Save a QualityAssessment
     * @param qualityAssessment QualityAssessment to be saved
     * @return
     */
    public QualityAssessment saveQualityAssessment(QualityAssessment qualityAssessment) {
        if (qualityAssessment.getExecutedAt() == null) {
            qualityAssessment.setExecutedAt(Instant.now());
        }
        return qualityAssessmentRepository.save(qualityAssessment);
    }

    /**
     * Update a QualityAssessment
     * @param qualityAssessmentId ID of the QualityAssessment
     * @param updatedQualityAssessment QualityAssessment to be updated
     * @return
     */
    public Optional<QualityAssessment> updateQualityAssessment(String qualityAssessmentId, QualityAssessment updatedQualityAssessment) {
        Optional<QualityAssessment> oldQualityAssessment = qualityAssessmentRepository.findById(qualityAssessmentId);
        if (oldQualityAssessment.isPresent()) {
            QualityAssessment assessment = oldQualityAssessment.get();
            assessment.setDatasetId(updatedQualityAssessment.getDatasetId());
            assessment.setQualityCriteriaId(updatedQualityAssessment.getQualityCriteriaId());
            assessment.setOverallResult(updatedQualityAssessment.getOverallResult());
            assessment.setSummary(updatedQualityAssessment.getSummary());
            assessment.setExecutedAt(updatedQualityAssessment.getExecutedAt());
            assessment.setExecutedBy(updatedQualityAssessment.getExecutedBy());
            QualityAssessment saved = qualityAssessmentRepository.save(assessment);
            return Optional.of(saved);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a QualityAssessment
     * @param qualityAssessmentId ID of QualityAssessment to be deleted
     * @return
     */
    public boolean deleteQualityAssessment(String qualityAssessmentId) {
        if (qualityAssessmentRepository.existsById(qualityAssessmentId)) {
            qualityAssessmentRepository.deleteById(qualityAssessmentId);
            return true;
        } else {
            return false;
        }
    }
}
