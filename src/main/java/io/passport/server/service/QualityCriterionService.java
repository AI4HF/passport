package io.passport.server.service;

import io.passport.server.model.QualityCriterion;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.QualityCriterionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for QualityCriterion management.
 */
@Service
public class QualityCriterionService {

    /**
     * QualityCriterion repo access for database management.
     */
    private final QualityCriterionRepository qualityCriterionRepository;
    private final RoleCheckerService roleCheckerService;

    /**
     * Lazy service references for limited use in cascade validation
     */
    @Autowired @Lazy private QualityCriterionAssessmentResultService qualityCriterionAssessmentResultService;

    @Autowired
    public QualityCriterionService(QualityCriterionRepository qualityCriterionRepository,
                                   RoleCheckerService roleCheckerService) {
        this.qualityCriterionRepository = qualityCriterionRepository;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Starts a validation chain of QualityCriterion and all of their children for cascades
     *
     * @param studyId Id of the Study
     * @param qualityCriterionId Id of the QualityCriterion
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateQualityCriterionDeletion(String studyId, String qualityCriterionId, Jwt principal) {
        List<ValidationResult> results = new ArrayList<>();

        results.add(qualityCriterionAssessmentResultService.validateCascade(
                studyId, "QualityCriterion", qualityCriterionId, principal));

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
        List<QualityCriterion> affectedCriteria;

        switch (sourceResourceType) {
            case "QualityCriteria":
                affectedCriteria = qualityCriterionRepository.findByQualityCriteriaId(sourceResourceId);
                break;
            case "Feature":
                affectedCriteria = qualityCriterionRepository.findByFeatureId(sourceResourceId);
                break;
            default:
                return new ValidationResult(true, "");
        }

        if (affectedCriteria.isEmpty()) {
            return new ValidationResult(true, "");
        }

        List<ValidationResult> childResults = new ArrayList<>();
        boolean authorized = true;

        for (QualityCriterion criterion : affectedCriteria) {
            boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                    studyId,
                    principal,
                    List.of(Role.DATA_ENGINEER)
            );

            if (!hasPermission) {
                authorized = false;
                break;
            }

            childResults.add(validateQualityCriterionDeletion(studyId, criterion.getQualityCriterionId(), principal));
        }

        if (!authorized) {
            return new ValidationResult(false, "QualityCriterion");
        }

        childResults.add(new ValidationResult(true, "QualityCriterion"));

        return ValidationResult.aggregate(childResults);
    }

    /**
     * Return all QualityCriterion rows of a criteria set
     * @param qualityCriteriaId ID of the QualityCriteria
     * @return
     */
    public List<QualityCriterion> findQualityCriterionByQualityCriteriaId(String qualityCriteriaId) {
        return qualityCriterionRepository.findByQualityCriteriaId(qualityCriteriaId);
    }

    /**
     * Find a QualityCriterion by qualityCriterionId
     * @param qualityCriterionId ID of the QualityCriterion
     * @return
     */
    public Optional<QualityCriterion> findQualityCriterionById(String qualityCriterionId) {
        return qualityCriterionRepository.findById(qualityCriterionId);
    }

    /**
     * Find the study a QualityCriterion belongs to
     * @param qualityCriterionId ID of the QualityCriterion
     * @return
     */
    public String findStudyIdByQualityCriterionId(String qualityCriterionId) {
        return qualityCriterionRepository.findStudyIdByQualityCriterionId(qualityCriterionId);
    }

    /**
     * Save a QualityCriterion
     * @param qualityCriterion QualityCriterion to be saved
     * @return
     */
    public QualityCriterion saveQualityCriterion(QualityCriterion qualityCriterion) {
        qualityCriterion.setCreatedAt(Instant.now());
        qualityCriterion.setLastUpdatedAt(Instant.now());
        return qualityCriterionRepository.save(qualityCriterion);
    }

    /**
     * Update a QualityCriterion
     * @param qualityCriterionId ID of the QualityCriterion
     * @param updatedQualityCriterion QualityCriterion to be updated
     * @return
     */
    public Optional<QualityCriterion> updateQualityCriterion(String qualityCriterionId, QualityCriterion updatedQualityCriterion) {
        Optional<QualityCriterion> oldQualityCriterion = qualityCriterionRepository.findById(qualityCriterionId);
        if (oldQualityCriterion.isPresent()) {
            QualityCriterion criterion = oldQualityCriterion.get();
            criterion.setQualityCriteriaId(updatedQualityCriterion.getQualityCriteriaId());
            criterion.setFeatureId(updatedQualityCriterion.getFeatureId());
            criterion.setName(updatedQualityCriterion.getName());
            criterion.setDescription(updatedQualityCriterion.getDescription());
            criterion.setCategory(updatedQualityCriterion.getCategory());
            criterion.setContext(updatedQualityCriterion.getContext());
            criterion.setSubcategory(updatedQualityCriterion.getSubcategory());
            criterion.setLanguage(updatedQualityCriterion.getLanguage());
            criterion.setExpression(updatedQualityCriterion.getExpression());
            criterion.setRuleExpression(updatedQualityCriterion.getRuleExpression());
            criterion.setLow(updatedQualityCriterion.getLow());
            criterion.setHigh(updatedQualityCriterion.getHigh());
            criterion.setLastUpdatedBy(updatedQualityCriterion.getLastUpdatedBy());
            criterion.setLastUpdatedAt(Instant.now());
            QualityCriterion saved = qualityCriterionRepository.save(criterion);
            return Optional.of(saved);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a QualityCriterion
     * @param qualityCriterionId ID of QualityCriterion to be deleted
     * @return
     */
    public boolean deleteQualityCriterion(String qualityCriterionId) {
        if (qualityCriterionRepository.existsById(qualityCriterionId)) {
            qualityCriterionRepository.deleteById(qualityCriterionId);
            return true;
        } else {
            return false;
        }
    }
}
