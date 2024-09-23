package io.passport.server.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
/**
 * DTO that encapsulates a LearningProcess and its associated LearningStages.
 */
public class LearningProcessWithStagesDTO {

    /**
     * The LearningProcess entity.
     */
    private LearningProcess learningProcess;

    /**
     * The list of LearningStages associated with the LearningProcess.
     */
    private List<LearningStage> learningStages;

    // Getters and Setters

    public LearningProcess getLearningProcess() {
        return learningProcess;
    }

    public void setLearningProcess(LearningProcess learningProcess) {
        this.learningProcess = learningProcess;
    }

    public List<LearningStage> getLearningStages() {
        return learningStages;
    }

    public void setLearningStages(List<LearningStage> learningStages) {
        this.learningStages = learningStages;
    }
}

