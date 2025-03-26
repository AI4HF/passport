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
     * Clear all old Experiment entries related to the study and create new ones.
     * Avoid deletion and recreation of Experiments with recurring Research Questions.
     * @param studyId ID of the study
     * @param experimentList list of experiment to be used in Experiment entries
     * @return
     */
    @Transactional
    public List<Experiment> createExperimentEntries(String studyId, List<Experiment> experimentList) {
        List<String> incomingQuestions = experimentList.stream()
                .map(Experiment::getResearchQuestion)
                .distinct()
                .toList();

        List<Experiment> existingExperiments = experimentRepository.findByStudyId(studyId);

        List<Experiment> toDelete = existingExperiments.stream()
                .filter(e -> !incomingQuestions.contains(e.getResearchQuestion()))
                .toList();
        experimentRepository.deleteAll(toDelete);

        List<String> existingQuestions = existingExperiments.stream()
                .map(Experiment::getResearchQuestion)
                .toList();

        List<Experiment> toInsert = experimentList.stream()
                .filter(e -> !existingQuestions.contains(e.getResearchQuestion()))
                .map(e -> {
                    Experiment newExperiment = new Experiment();
                    newExperiment.setStudyId(studyId);
                    newExperiment.setResearchQuestion(e.getResearchQuestion());
                    return newExperiment;
                })
                .toList();

        return experimentRepository.saveAll(toInsert);
    }

    /**
     * Delete all Experiment entries related to the study.
     * @param studyId ID of the study
     * @return
     */
    @Transactional
    public void clearExperimentEntriesByStudyId(String studyId) {
        experimentRepository.deleteAllByStudyId(studyId);
    }
}
