package io.passport.server.controller;

import io.passport.server.model.Experiment;
import io.passport.server.service.ExperimentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Class which stores the generated HTTP requests related to experiment operations.
 */
@RestController
@RequestMapping("/experiment")
public class ExperimentController {

    private static final Logger log = LoggerFactory.getLogger(ExperimentController.class);

    /**
     * Experiment service for experiment management
     */
    private final ExperimentService experimentService;

    @Autowired
    public ExperimentController(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    /**
     * Read experiments by studyId
     * @param studyId ID of the study related to experiment.
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<Experiment>> getExperimentsByStudyId(@RequestParam("studyId") Long studyId) {

        List<Experiment> experiment = this.experimentService.findExperimentByStudyId(studyId);

        return ResponseEntity.ok().body(experiment);
    }

    /**
     * Clear all old Experiment entries related to the study and create new ones. Return updated experiment list.
     * @param studyId ID of the study.
     * @param experiments List of experiment to be used in experiment entries
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createExperiments(@RequestParam Long studyId, @RequestBody List<Experiment> experiments) {
        try{
            this.experimentService.createExperimentEntries(studyId, experiments);
            List<Experiment> newExperiments = this.experimentService.findExperimentByStudyId(studyId);
            return ResponseEntity.status(HttpStatus.CREATED).body(newExperiments);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
