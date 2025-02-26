package io.passport.server.service;

import io.passport.server.model.LearningProcess;
import io.passport.server.repository.LearningProcessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for learning process management.
 */
@Service
public class LearningProcessService {

    /**
     * LearningProcess repo access for database management.
     */
    private final LearningProcessRepository learningProcessRepository;

    @Autowired
    public LearningProcessService(LearningProcessRepository learningProcessRepository) {
        this.learningProcessRepository = learningProcessRepository;
    }

    /**
     * Return all learning processes
     * @return
     */
    public List<LearningProcess> getAllLearningProcesses() {
        return learningProcessRepository.findAll();
    }

    /**
     * Return all LearningProcess by studyId
     * @param studyId ID of the study
     * @return
     */
    public List<LearningProcess> getAllLearningProcessByStudyId(String studyId) {
        return learningProcessRepository.findAllByStudyId(studyId);
    }

    /**
     * Find a learning process by learningProcessId
     * @param learningProcessId ID of the learning process
     * @return
     */
    public Optional<LearningProcess> findLearningProcessById(String learningProcessId) {
        return learningProcessRepository.findById(learningProcessId);
    }

    /**
     * Save a learning process
     * @param learningProcess learning process to be saved
     * @return
     */
    public LearningProcess saveLearningProcess(LearningProcess learningProcess) {
        return learningProcessRepository.save(learningProcess);
    }

    /**
     * Update a learning process
     * @param learningProcessId ID of the learning process
     * @param updatedLearningProcess learning process to be updated
     * @return
     */
    public Optional<LearningProcess> updateLearningProcess(String learningProcessId, LearningProcess updatedLearningProcess) {
        Optional<LearningProcess> oldLearningProcess = learningProcessRepository.findById(learningProcessId);
        if (oldLearningProcess.isPresent()) {
            LearningProcess learningProcess = oldLearningProcess.get();
            learningProcess.setImplementationId(updatedLearningProcess.getImplementationId());
            learningProcess.setDescription(updatedLearningProcess.getDescription());
            LearningProcess savedLearningProcess = learningProcessRepository.save(learningProcess);
            return Optional.of(savedLearningProcess);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a learning process
     * @param learningProcessId ID of learning process to be deleted
     * @return
     */
    public Optional<LearningProcess> deleteLearningProcess(String learningProcessId) {
        Optional<LearningProcess> existingLearningProcess = learningProcessRepository.findById(learningProcessId);
        if (existingLearningProcess.isPresent()) {
            learningProcessRepository.delete(existingLearningProcess.get());
            return existingLearningProcess;
        } else {
            return Optional.empty();
        }
    }

}
