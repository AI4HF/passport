package io.passport.server.controller;

import io.passport.server.model.Population;
import io.passport.server.service.PopulationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Class which stores the generated HTTP requests related to population operations.
 */
@RestController
@RequestMapping("/population")
public class PopulationController {

    private static final Logger log = LoggerFactory.getLogger(PopulationController.class);

    /**
     * Population service for population management
     */
    private final PopulationService populationService;

    @Autowired
    public PopulationController(PopulationService populationService) {
        this.populationService = populationService;
    }

    /**
     * Read population by populationId
     * @param populationId ID of the population.
     * @return
     */
    @GetMapping("/{populationId}")
    public ResponseEntity<?> getPopulationById(@PathVariable("populationId") Long populationId) {

        Optional<Population> population = this.populationService.findPopulationById(populationId);

        if(population.isPresent()) {
            return ResponseEntity.ok().body(population);
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Read population by studyId
     * @param studyId ID of the study related to population.
     * @return
     */
    @GetMapping()
    public ResponseEntity<?> getPopulationByStudyId(@RequestParam("studyId") Long studyId) {

        Optional<Population> population = this.populationService.findPopulationByStudyId(studyId);

        if(population.isPresent()) {
            return ResponseEntity.ok().body(population);
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create Population.
     * @param population Population model instance to be created.
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createPopulation(@RequestBody Population population) {
        try{
            Population savedPopulation = this.populationService.savePopulation(population);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPopulation);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Population.
     * @param populationId ID of the population that is to be updated.
     * @param updatedPopulation model instance with updated details.
     * @return
     */
    @PutMapping("/{populationId}")
    public ResponseEntity<?> updatePopulation(@PathVariable Long populationId, @RequestBody Population updatedPopulation) {
        try{
            Optional<Population> savedPopulation = this.populationService.updatePopulation(populationId, updatedPopulation);
            if(savedPopulation.isPresent()) {
                return ResponseEntity.ok(savedPopulation.get());
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }

    /**
     * Delete by Population ID.
     * @param populationId ID of the population that is to be deleted.
     * @return
     */
    @DeleteMapping("/{populationId}")
    public ResponseEntity<Object> deletePopulation(@PathVariable Long populationId) {
        try{
            boolean isDeleted = this.populationService.deletePopulation(populationId);
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
