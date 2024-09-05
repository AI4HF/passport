package io.passport.server.controller;

import io.passport.server.model.LearningStage;
import io.passport.server.service.LearningStageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Class which stores the generated HTTP requests related to learning stage operations.
 */
@RestController
@RequestMapping("/learning-stage")
public class LearningStageController {
    private static final Logger log = LoggerFactory.getLogger(LearningStageController.class);
    /**
     * LearningStage service for learning stage management
     */
    private final LearningStageService learningStageService;

    @Autowired
    public LearningStageController(LearningStageService learningStageService) {
        this.learningStageService = learningStageService;
    }

    /**
     * Retrieves learning stages. If a learningProcessId is provided, it filters by that process ID; otherwise, it retrieves all learning stages.
     * @param learningProcessId the ID of the learning process (optional)
     * @return a list of learning stages
     */
    @GetMapping
    public ResponseEntity<List<LearningStage>> getLearningStages(
            @RequestParam(required = false) Long learningProcessId) {
        List<LearningStage> learningStages;

        if (learningProcessId != null) {
            learningStages = learningStageService.findLearningStagesByProcessId(learningProcessId);
        } else {
            learningStages = learningStageService.getAllLearningStages();
        }

        return ResponseEntity.ok(learningStages);
    }


    /**
     * Read a learning stage by id
     * @param learningStageId ID of the learning stage
     * @return
     */
    @GetMapping("/{learningStageId}")
    public ResponseEntity<?> getLearningStage(@PathVariable Long learningStageId) {
        Optional<LearningStage> learningStage = this.learningStageService.findLearningStageById(learningStageId);

        if(learningStage.isPresent()) {
            return ResponseEntity.ok().body(learningStage.get());
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create LearningStage.
     * @param learningStage LearningStage model instance to be created.
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createLearningStage(@RequestBody LearningStage learningStage) {
        try{
            LearningStage savedLearningStage = this.learningStageService.saveLearningStage(learningStage);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedLearningStage);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update LearningStage.
     * @param learningStageId ID of the learning stage that is to be updated.
     * @param updatedLearningStage LearningStage model instance with updated details.
     * @return
     */
    @PutMapping("/{learningStageId}")
    public ResponseEntity<?> updateLearningStage(@PathVariable Long learningStageId, @RequestBody LearningStage updatedLearningStage) {
        try{
            Optional<LearningStage> savedLearningStage = this.learningStageService.updateLearningStage(learningStageId, updatedLearningStage);
            if(savedLearningStage.isPresent()) {
                return ResponseEntity.ok().body(savedLearningStage);
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by LearningStage ID.
     * @param learningStageId ID of the learning stage that is to be deleted.
     * @return
     */
    @DeleteMapping("/{learningStageId}")
    public ResponseEntity<?> deleteLearningStage(@PathVariable Long learningStageId) {
        try{
            boolean isDeleted = this.learningStageService.deleteLearningStage(learningStageId);
            if(isDeleted) {
                return ResponseEntity.noContent().build();
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
