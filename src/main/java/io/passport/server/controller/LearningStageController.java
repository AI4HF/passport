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
     * Read all learning stages
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<LearningStage>> getAllLearningStages() {
        List<LearningStage> learningStages = this.learningStageService.getAllLearningStages();

        long totalCount = learningStages.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(learningStages);
    }

    /**
     * Read a learning stage by id
     * @param learningStageId ID of the learning stage
     * @return
     */
    @GetMapping("/{learningStageId}")
    public ResponseEntity<?> getLearningStage(@PathVariable String learningStageId) {
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
    public ResponseEntity<?> updateLearningStage(@PathVariable String learningStageId, @RequestBody LearningStage updatedLearningStage) {
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
    public ResponseEntity<?> deleteLearningStage(@PathVariable String learningStageId) {
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
