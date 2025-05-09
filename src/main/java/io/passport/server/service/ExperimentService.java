package io.passport.server.service;

import io.passport.server.model.Experiment;
import io.passport.server.repository.ExperimentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
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
     * Find all experiments for an assigned personnel
     * @param personnelId ID of the personnel
     * @return
     */
    public List<Experiment> findAllExperiments(String personnelId) {
        return this.experimentRepository.findExperimentsByPersonnelId(personnelId);
    }

    /**
     * Find an experiment by studyId
     * @param studyId ID of the study
     * @return
     */
    public List<Experiment> findExperimentByStudyId(String studyId) {
        return this.experimentRepository.findByStudyId(studyId);
    }


    /**
     * Create new experiments
     * @param studyId ID of the study
     * @param experimentList list of experiment to be used in Experiment entries
     * @return
     */
    @Transactional
    public List<Experiment> createExperimentEntries(String studyId, List<Experiment> experimentList) {

        List<Experiment> ExperimentEntries = experimentList.stream().map((experiment -> {
            Experiment newExperiment = new Experiment();
            newExperiment.setExperimentId(experiment.getExperimentId());
            newExperiment.setStudyId(studyId);
            newExperiment.setResearchQuestion(experiment.getResearchQuestion());
            return newExperiment;
        })).collect(Collectors.toList());

        return experimentRepository.saveAll(ExperimentEntries);
    }
}
