package io.passport.server.service;

import io.passport.server.model.Experiment;
import io.passport.server.repository.ExperimentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for experiment management.
 */
@Service
public class ExperimentService {

    /**
     * Experiment repo access for database management.
     */
    public final ExperimentRepository experimentRepository;

    @Autowired
    public ExperimentService(ExperimentRepository experimentRepository) {
        this.experimentRepository = experimentRepository;
    }

    /**
     * Find an experiment by studyId
     * @param studyId ID of the study
     * @return
     */
    public List<Experiment> findExperimentByStudyId(Long studyId) {
        return this.experimentRepository.findByStudyId(studyId);
    }


    /**
     * Clear all old Experiment entries related to the study and create new ones.
     * @param studyId ID of the study
     * @param experimentList list of experiment to be used in Experiment entries
     * @return
     */
    @Transactional
    public void createExperimentEntries(Long studyId, List<Experiment> experimentList) {
        // Clear existing entries
        clearExperimentEntriesByStudyId(studyId);

        List<Experiment> ExperimentEntries = experimentList.stream().map((experiment -> {
            Experiment newExperiment = new Experiment();
            newExperiment.setStudyId(studyId);
            newExperiment.setResearchQuestion(experiment.getResearchQuestion());
            return newExperiment;
        })).collect(Collectors.toList());

        experimentRepository.saveAll(ExperimentEntries);
    }

    /**
     * Delete all Experiment entries related to the study.
     * @param studyId ID of the study
     * @return
     */
    @Transactional
    public void clearExperimentEntriesByStudyId(Long studyId) {
        experimentRepository.deleteAllByStudyId(studyId);
    }
}
