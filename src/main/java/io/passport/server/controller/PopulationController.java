package io.passport.server.controller;

import io.passport.server.model.Population;
import io.passport.server.repository.PopulationRepository;
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
     * Population repo access for database management.
     */
    private final PopulationRepository populationRepository;

    @Autowired
    public PopulationController(PopulationRepository populationRepository) {
        this.populationRepository = populationRepository;
    }

    @GetMapping("/{studyId}")
    public ResponseEntity<?> getPopulationByStudyId(@PathVariable("studyId") Long studyId) {

        Optional<Population> population = populationRepository.findByStudyId(studyId);

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
    @PostMapping("/")
    public ResponseEntity<?> createPopulation(@RequestBody Population population) {
        try{
            Population savedPopulation = populationRepository.save(population);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPopulation);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Population.
     * @param id ID of the population that is to be updated.
     * @param updatedPopulation model instance with updated details.
     * @return
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePopulation(@PathVariable Long id, @RequestBody Population updatedPopulation) {
        Optional<Population> optionalPopulation = populationRepository.findById(id);
        if (optionalPopulation.isPresent()) {
            Population population = optionalPopulation.get();
            population.setPopulationUrl(updatedPopulation.getPopulationUrl());
            population.setDescription(updatedPopulation.getDescription());
            population.setCharacteristics(updatedPopulation.getCharacteristics());
            population.setStudyId(updatedPopulation.getStudyId());
            try{
                Population savedPopulation = populationRepository.save(population);
                return ResponseEntity.ok(savedPopulation);
            }catch(Exception e){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete by Population ID.
     * @param populationId ID of the population that is to be deleted.
     * @return
     */
    @DeleteMapping("/{populationId}")
    public ResponseEntity<Object> deletePopulation(@PathVariable Long populationId) {
        return populationRepository.findById(populationId)
                .map(population -> {
                    populationRepository.delete(population);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
