package io.passport.server.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.passport.server.model.Organization;
import io.passport.server.model.Study;
import io.passport.server.model.StudyOrganization;
import io.passport.server.model.StudyOrganizationId;
import io.passport.server.repository.StudyOrganizationRepository;
import jakarta.transaction.Transactional;

/**
 * Service class for StudyOrganization management.
 */
@Service
public class StudyOrganizationService {

    /**
     * StudyOrganization repo access for database management.
     */
    private final StudyOrganizationRepository studyOrganizationRepository;

    /**
     * StudyPersonnel service for StudyPersonnel related operations.
     */
    private final StudyPersonnelService studyPersonnelService;

    @Autowired
    public StudyOrganizationService(StudyOrganizationRepository studyOrganizationRepository, StudyPersonnelService studyPersonnelService) {
        this.studyOrganizationRepository = studyOrganizationRepository;
        this.studyPersonnelService = studyPersonnelService;
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
