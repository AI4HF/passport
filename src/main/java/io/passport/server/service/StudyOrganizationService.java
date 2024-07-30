package io.passport.server.service;

import io.passport.server.model.*;
import io.passport.server.repository.StudyOrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for StudyOrganization management.
 */
@Service
public class StudyOrganizationService {

    /**
     * StudyPersonnel repo access for database management.
     */
    private final StudyOrganizationRepository studyOrganizationRepository;

    @Autowired
    public StudyOrganizationService(StudyOrganizationRepository studyOrganizationRepository) {
        this.studyOrganizationRepository = studyOrganizationRepository;
    }

    /**
     * Get all organizations related to the study.
     * @param studyId ID of the study
     * @return
     */
    public List<Organization> findOrganizationsByStudyId(Long studyId) {
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
    public List<Study> findStudiesByOrganizationId(Long organizationId) {
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
    public boolean deleteStudyOrganization(StudyOrganizationId studyOrganizationId) {
        if(studyOrganizationRepository.existsById(studyOrganizationId)) {
            studyOrganizationRepository.deleteById(studyOrganizationId);
            return true;
        }else{
            return false;
        }
    }
}
