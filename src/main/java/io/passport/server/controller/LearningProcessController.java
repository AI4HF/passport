package io.passport.server.controller;

import io.passport.server.model.LearningProcess;
import io.passport.server.service.LearningProcessService;
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
 * Class which stores the generated HTTP requests related to learning process operations.
 */
@RestController
@RequestMapping("/learning-process")
public class LearningProcessController {
    private static final Logger log = LoggerFactory.getLogger(LearningProcessController.class);
    /**
     * LearningProcess service for learning process management
     */
    private final LearningProcessService learningProcessService;

    @Autowired
    public LearningProcessController(LearningProcessService learningProcessService) {
        this.learningProcessService = learningProcessService;
    }

    /**
     * Read all learning processes
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<LearningProcess>> getAllLearningProcesses() {
        List<LearningProcess> learningProcesses = this.learningProcessService.getAllLearningProcesses();

        long totalCount = learningProcesses.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(learningProcesses);
    }

    /**
     * Read a learning process by id
     * @param learningProcessId ID of the learning process
     * @return
     */
    @GetMapping("/{learningProcessId}")
    public ResponseEntity<?> getLearningProcess(@PathVariable Long learningProcessId) {
        Optional<LearningProcess> learningProcess = this.learningProcessService.findLearningProcessById(learningProcessId);

        if(learningProcess.isPresent()) {
            return ResponseEntity.ok().body(learningProcess.get());
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create LearningProcess.
     * @param learningProcess LearningProcess model instance to be created.
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createLearningProcess(@RequestBody LearningProcess learningProcess) {
        try{
            LearningProcess savedLearningProcess = this.learningProcessService.saveLearningProcess(learningProcess);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedLearningProcess);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update LearningProcess.
     * @param learningProcessId ID of the learning process that is to be updated.
     * @param updatedLearningProcess LearningProcess model instance with updated details.
     * @return
     */
    @PutMapping("/{learningProcessId}")
    public ResponseEntity<?> updateLearningProcess(@PathVariable Long learningProcessId, @RequestBody LearningProcess updatedLearningProcess) {
        try{
            Optional<LearningProcess> savedLearningProcess = this.learningProcessService.updateLearningProcess(learningProcessId, updatedLearningProcess);
            if(savedLearningProcess.isPresent()) {
                return ResponseEntity.ok().body(savedLearningProcess);
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by LearningProcess ID.
     * @param learningProcessId ID of the learning process that is to be deleted.
     * @return
     */
    @DeleteMapping("/{learningProcessId}")
    public ResponseEntity<?> deleteLearningProcess(@PathVariable Long learningProcessId) {
        try{
            boolean isDeleted = this.learningProcessService.deleteLearningProcess(learningProcessId);
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
