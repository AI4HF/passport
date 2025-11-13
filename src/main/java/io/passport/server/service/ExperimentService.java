package io.passport.server.service;

import io.passport.server.model.Experiment;
import io.passport.server.repository.ExperimentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
    public List<Experiment> findExperimentByStudyId(String studyId) {
        return this.experimentRepository.findByStudyId(studyId);
    }

    /**
     * Overwrite all Experiment entries for a Study
     * @param studyId ID of the study
     * @param incoming Collection of Experiments to be overwritten as
     * @return Final state of overwritten Experiments
     */
    @Transactional
    public List<Experiment> replaceExperiments(String studyId, List<Experiment> incoming) {
        Set<String> incomingIds = incoming.stream()
                .map(Experiment::getExperimentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (incomingIds.isEmpty()) {
            experimentRepository.deleteAllByStudyId(studyId);
            return Collections.emptyList();
        }

        experimentRepository.deleteByStudyIdAndExperimentIdNotIn(studyId, incomingIds);

        List<Experiment> toSave = incoming.stream()
                .map(exp -> {
                    Experiment e = new Experiment();
                    e.setExperimentId(exp.getExperimentId());
                    e.setStudyId(studyId);
                    e.setResearchQuestion(exp.getResearchQuestion());
                    return e;
                })
                .collect(Collectors.toList());

        return experimentRepository.saveAll(toSave);
    }
}
