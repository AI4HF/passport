package io.passport.server.service;

import io.passport.server.model.*;
import io.passport.server.repository.StudyOrganizationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for StudyOrganization management.
 */
@Service
public class StudyOrganizationService {

    /**
     * StudyOrganization repo access for database management.
     */
    private final StudyOrganizationRepository studyOrganizationRepository;
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public StudyOrganizationService(StudyOrganizationRepository studyOrganizationRepository,
                                    RoleCheckerService roleCheckerService) {
        this.studyOrganizationRepository = studyOrganizationRepository;
        this.roleCheckerService = roleCheckerService;
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
        List<StudyOrganization> affectedLinks;

        switch (sourceResourceType) {
            case "Study":
                affectedLinks = studyOrganizationRepository.findByIdStudyId(sourceResourceId);
                break;
            case "Organization":
                affectedLinks = studyOrganizationRepository.findByIdOrganizationId(sourceResourceId);
                break;
            case "Population":
                affectedLinks = studyOrganizationRepository.findByPopulationId(sourceResourceId);
                break;
            default:
                return new ValidationResult(true, "");
        }

        if (affectedLinks.isEmpty()) {
            return new ValidationResult(true, "");
        }

        boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                studyId,
                principal,
                List.of(Role.STUDY_OWNER)
        );

        if (!hasPermission) {
            return new ValidationResult(false, "StudyOrganization");
        }

        return new ValidationResult(true, "StudyOrganization");
    }

    /**
     * Get all organizations related to the study.
     * @param studyId ID of the study
     * @return
     */
    public List<Organization> findOrganizationsByStudyId(String studyId) {
        return this.studyOrganizationRepository.findOrganizationsByStudyId(studyId);
    }

    /**
     * Get a studyOrganization by studyOrganizationId.
     * @param studyOrganizationId ID of the studyOrganization
     * @return
     */
    public Optional<StudyOrganization> findStudyOrganizationById(StudyOrganizationId studyOrganizationId) {
        return this.studyOrganizationRepository.findById(studyOrganizationId);
    }

    /**
     * Get all studies related to the organization.
     * @param organizationId ID of the organization
     * @return
     */
    public List<Study> findStudiesByOrganizationId(String organizationId) {
        return this.studyOrganizationRepository.findStudiesByOrganizationId(organizationId);
    }

    /**
     * Save a study organization object into database.
     * @param studyOrganization studyOrganization object that will be saved
     * @return
     */
    public StudyOrganization createStudyOrganizationEntries(StudyOrganization studyOrganization) {
        return this.studyOrganizationRepository.save(studyOrganization);
    }

    /**
     * Update a study organization object
     * @param studyOrganizationId ID of the study organization
     * @param updatedStudyOrganization studyOrganization to be updated
     * @return
     */
    @Transactional
    public Optional<StudyOrganization> updateStudyOrganization(StudyOrganizationId studyOrganizationId, StudyOrganization updatedStudyOrganization) {
        Optional<StudyOrganization> oldStudyOrganization = this.studyOrganizationRepository.findById(studyOrganizationId);
        if (oldStudyOrganization.isPresent()) {
            StudyOrganization studyOrganization = oldStudyOrganization.get();
            studyOrganization.setRole(updatedStudyOrganization.getRole());
            studyOrganization.setPopulationId(updatedStudyOrganization.getPopulationId());
            studyOrganization.setResponsiblePersonnelId(updatedStudyOrganization.getResponsiblePersonnelId());
            StudyOrganization savedStudyOrganization = this.studyOrganizationRepository.save(studyOrganization);
            return Optional.of(savedStudyOrganization);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a study organization
     * @param studyOrganizationId ID of study organization to be deleted
     * @return
     */
    public Optional<StudyOrganization> deleteStudyOrganization(StudyOrganizationId studyOrganizationId) {
        Optional<StudyOrganization> existingOrganization = studyOrganizationRepository.findById(studyOrganizationId);
        if (existingOrganization.isPresent()) {
            studyOrganizationRepository.delete(existingOrganization.get());
            return existingOrganization;
        } else {
            return Optional.empty();
        }
    }

}
