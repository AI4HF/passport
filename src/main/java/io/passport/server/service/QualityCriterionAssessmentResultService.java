package io.passport.server.service;

import io.passport.server.model.QualityCriterionAssessmentResult;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.QualityCriterionAssessmentResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for QualityCriterionAssessmentResult management.
 */
@Service
public class QualityCriterionAssessmentResultService {

    /**
     * QualityCriterionAssessmentResult repo access for database management.
     */
    private final QualityCriterionAssessmentResultRepository resultRepository;
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public QualityCriterionAssessmentResultService(QualityCriterionAssessmentResultRepository resultRepository,
                                                   RoleCheckerService roleCheckerService) {
        this.resultRepository = resultRepository;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Determines which entities are to be cascaded based on the request from the previous element in the chain.
     * Results are leaves of the chain, so this ends it.
     *
     * @param studyId Id of the Study
     * @param sourceResourceType Resource type of the parent element in the Cascade chain
     * @param sourceResourceId Resource id of the parent element in the Cascade chain
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateCascade(String studyId, String sourceResourceType, String sourceResourceId, Jwt principal) {
        List<QualityCriterionAssessmentResult> affectedResults;

        switch (sourceResourceType) {
            case "QualityAssessment":
                affectedResults = resultRepository.findByQualityAssessmentId(sourceResourceId);
                break;
            case "QualityCriterion":
                affectedResults = resultRepository.findByQualityCriterionId(sourceResourceId);
                break;
            default:
                return new ValidationResult(true, "");
        }

        if (affectedResults.isEmpty()) {
            return new ValidationResult(true, "");
        }

        boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                studyId,
                principal,
                List.of(Role.DATA_ENGINEER)
        );

        if (!hasPermission) {
            return new ValidationResult(false, "QualityCriterionAssessmentResult");
        }

        return new ValidationResult(true, "QualityCriterionAssessmentResult");
    }

    /**
     * Return all results of an assessment run
     * @param qualityAssessmentId ID of the QualityAssessment
     * @return
     */
    public List<QualityCriterionAssessmentResult> findResultsByQualityAssessmentId(String qualityAssessmentId) {
        return resultRepository.findByQualityAssessmentId(qualityAssessmentId);
    }

    /**
     * Find a result by resultId
     * @param resultId ID of the result
     * @return
     */
    public Optional<QualityCriterionAssessmentResult> findResultById(String resultId) {
        return resultRepository.findById(resultId);
    }

    /**
     * Save a result
     * @param result result to be saved
     * @return
     */
    public QualityCriterionAssessmentResult saveResult(QualityCriterionAssessmentResult result) {
        return resultRepository.save(result);
    }

    /**
     * Update a result
     * @param resultId ID of the result
     * @param updatedResult result to be updated
     * @return
     */
    public Optional<QualityCriterionAssessmentResult> updateResult(String resultId, QualityCriterionAssessmentResult updatedResult) {
        Optional<QualityCriterionAssessmentResult> oldResult = resultRepository.findById(resultId);
        if (oldResult.isPresent()) {
            QualityCriterionAssessmentResult result = oldResult.get();
            result.setQualityAssessmentId(updatedResult.getQualityAssessmentId());
            result.setQualityCriterionId(updatedResult.getQualityCriterionId());
            result.setValue(updatedResult.getValue());
            result.setResult(updatedResult.getResult());
            result.setDetail(updatedResult.getDetail());
            QualityCriterionAssessmentResult saved = resultRepository.save(result);
            return Optional.of(saved);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a result
     * @param resultId ID of the result to be deleted
     * @return
     */
    public boolean deleteResult(String resultId) {
        if (resultRepository.existsById(resultId)) {
            resultRepository.deleteById(resultId);
            return true;
        } else {
            return false;
        }
    }
}
