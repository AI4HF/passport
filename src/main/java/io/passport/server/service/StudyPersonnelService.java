package io.passport.server.service;

import io.passport.server.model.Personnel;
import io.passport.server.model.StudyPersonnel;
import io.passport.server.model.StudyPersonnelId;
import io.passport.server.repository.StudyPersonnelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for StudyPersonnel management.
 */
@Service
public class StudyPersonnelService {

    /**
     * StudyPersonnel repo access for database management.
     */
    private final StudyPersonnelRepository studyPersonnelRepository;

    /**
     * Personnel service for personnel details
     */
    private final PersonnelService personnelService;

    @Autowired
    public StudyPersonnelService(StudyPersonnelRepository studyPersonnelRepository, PersonnelService personnelService) {
        this.studyPersonnelRepository = studyPersonnelRepository;
        this.personnelService = personnelService;
    }

    /**
     * Get all personnel related to the study and organization.
     * @param studyId ID of the study
     * @param organizationId ID of the organization
     * @return
     */
    public List<Personnel> findPersonnelByStudyIdAndOrganizationId(Long studyId, Long organizationId){
        return this.studyPersonnelRepository.findPersonnelByStudyIdAndOrganizationId(studyId, organizationId);
    }

    /**
     * Delete StudyPersonnel entries by studyId and PersonnelId.
     * @param studyId ID of the study
     * @param personnelIdList ID list for personnel
     * @return
     */
    @Transactional
    public void clearStudyPersonnelEntriesByStudyIdAndPersonnelId(Long studyId, List<String> personnelIdList) {
        studyPersonnelRepository.deleteByStudyIdAndPersonnelId(studyId, personnelIdList);
    }

    /**
     * Clear StudyPersonnel entries by studyId and PersonnelId then create new ones.
     * @param studyId ID of the study
     * @param organizationId ID of the organization
     * @param personnelList list of personnel to be used in StudyPersonnel entries
     * @return
     */
    @Transactional
    public void createStudyPersonnelEntries(Long studyId, Long organizationId, List<Personnel> personnelList) {
        // Fetch existing personnel for organization and clear them from StudyPersonnel table
        List<String> personnelIdList = this.personnelService.findPersonnelByOrganizationId(organizationId).stream()
                .map(Personnel::getPersonId).collect(Collectors.toList());
        clearStudyPersonnelEntriesByStudyIdAndPersonnelId(studyId, personnelIdList);

        List<StudyPersonnel> studyPersonnelEntries = personnelList.stream().map((personnel -> {
            StudyPersonnel studyPersonnel = new StudyPersonnel();
            StudyPersonnelId studyPersonnelId = new StudyPersonnelId();
            studyPersonnelId.setStudyId(studyId);
            studyPersonnelId.setPersonnelId(personnel.getPersonId());
            studyPersonnel.setId(studyPersonnelId);
            studyPersonnel.setRole(personnel.getRole());
            return studyPersonnel;
        })).collect(Collectors.toList());

        studyPersonnelRepository.saveAll(studyPersonnelEntries);
    }
}
