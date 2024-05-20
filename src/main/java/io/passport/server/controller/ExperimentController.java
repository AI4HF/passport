package io.passport.server.controller;

import io.passport.server.model.Experiment;
import io.passport.server.repository.ExperimentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Class which stores the generated HTTP requests related to experiment operations.
 */
@RestController
@RequestMapping("/experiments")
public class ExperimentController {
    /**
     * Experiment repo access for database management.
     */
    private final ExperimentRepository experimentRepository;

    @Autowired
    public ExperimentController(ExperimentRepository experimentRepository) {
        this.experimentRepository = experimentRepository;
    }

    @GetMapping("/")
    public ResponseEntity<List<Experiment>> getAllExperiments(
            @RequestParam Long studyId,
            @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, 10);
        Page<Experiment> experimentPage = experimentRepository.findByStudyId(studyId, pageable);

        List<Experiment> experimentList = experimentPage.getContent();
        long totalCount = experimentPage.getTotalElements();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(experimentList);
    }

    /**
     * Create Experiment.
     * @param experiment Experiment model instance to be created.
     * @return
     */
    @PostMapping("/")
    public ResponseEntity<Experiment> createExperiment(@RequestBody Experiment experiment) {
        Experiment savedExperiment = experimentRepository.save(experiment);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedExperiment);
    }

    /**
     * Delete by Experiment ID.
     * @param experimentId ID of the experiment that is to be deleted.
     * @return
     */
    @DeleteMapping("/{experimentId}")
    public ResponseEntity<Object> deleteExperiment(@PathVariable Long experimentId) {
        return experimentRepository.findById(experimentId)
                .map(experiment -> {
                    experimentRepository.delete(experiment);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
