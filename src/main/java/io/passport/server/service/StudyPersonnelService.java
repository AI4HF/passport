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

    @Autowired
    public StudyPersonnelService(StudyPersonnelRepository studyPersonnelRepository) {
        this.studyPersonnelRepository = studyPersonnelRepository;
    }

    /**
     * Get all personnel related to the study.
     * @param studyId ID of the study
     * @return
     */
    public List<Personnel> findPersonnelByStudyId(Long studyId){
        return this.studyPersonnelRepository.findPersonnelByStudyId(studyId);
    }

    /**
     * Delete all StudyPersonnel entries related to the study.
     * @param studyId ID of the study
     * @return
     */
    @Transactional
    public void clearStudyPersonnelEntriesByStudyId(Long studyId) {
        studyPersonnelRepository.deleteByStudyId(studyId);
    }

    /**
     * Clear all old StudyPersonnel entries related to the study and create new ones.
     * @param studyId ID of the study
     * @return
     */
    @Transactional
    public void createStudyPersonnelEntries(Long studyId, List<Personnel> personnelList) {
        // Clear existing entries
        clearStudyPersonnelEntriesByStudyId(studyId);

        List<StudyPersonnel> studyPersonnelEntries = personnelList.stream().map((personnel -> {
            StudyPersonnel studyPersonnel = new StudyPersonnel();
            StudyPersonnelId studyPersonnelId = new StudyPersonnelId();
            studyPersonnelId.setStudyId(studyId);
            studyPersonnelId.setPersonnelId(personnel.getId());
            studyPersonnel.setId(studyPersonnelId);
            studyPersonnel.setRole(personnel.getRole());
            return studyPersonnel;
        })).collect(Collectors.toList());

        studyPersonnelRepository.saveAll(studyPersonnelEntries);
    }
}
