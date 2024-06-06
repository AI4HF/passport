package io.passport.server.controller;

import io.passport.server.model.Population;
import io.passport.server.repository.PopulationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Class which stores the generated HTTP requests related to population operations.
 */
@RestController
@RequestMapping("/populations")
public class PopulationController {
    /**
     * Population repo access for database management.
     */
    private final PopulationRepository populationRepository;

    @Autowired
    public PopulationController(PopulationRepository populationRepository) {
        this.populationRepository = populationRepository;
    }

    @GetMapping("/")
    public ResponseEntity<List<Population>> getAllPopulations(
            @RequestParam Long studyId,
            @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, 10);
        Page<Population> populationPage = populationRepository.findByStudyId(studyId, pageable);

        List<Population> populationList = populationPage.getContent();
        long totalCount = populationPage.getTotalElements();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(populationList);
    }

    /**
     * Create Population.
     * @param population Population model instance to be created.
     * @return
     */
    @PostMapping("/")
    public ResponseEntity<Population> createPopulation(@RequestBody Population population) {
        Population savedPopulation = populationRepository.save(population);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPopulation);
    }

    /**
     * Update Population.
     * @param id ID of the population that is to be updated.
     * @param updatedPopulation model instance with updated details.
     * @return
     */
    @PutMapping("/{id}")
    public ResponseEntity<Population> updatePopulation(@PathVariable Long id, @RequestBody Population updatedPopulation) {
        Optional<Population> optionalPopulation = populationRepository.findById(id);
        if (optionalPopulation.isPresent()) {
            Population population = optionalPopulation.get();
            population.setPopulationURL(updatedPopulation.getPopulationURL());
            population.setResearchQuestion(updatedPopulation.getResearchQuestion());
            population.setCharacteristics(updatedPopulation.getCharacteristics());

            Population savedPopulation = populationRepository.save(population);
            return ResponseEntity.ok(savedPopulation);
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
