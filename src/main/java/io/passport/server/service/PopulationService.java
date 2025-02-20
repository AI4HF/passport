package io.passport.server.service;

import io.passport.server.model.Population;
import io.passport.server.repository.PopulationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.List;
import java.util.Optional;

/**
 * Service class for population management.
 */
@Service
public class PopulationService {

    /**
     * Population repo access for database management.
     */
    private final PopulationRepository populationRepository;

    @Autowired
    public PopulationService(PopulationRepository populationRepository) {
        this.populationRepository = populationRepository;
    }

    public List<Population> findAllPopulations() {
        return populationRepository.findAll();
    }

    /**
     * Find a population by populationId
     * @param populationId ID of the population
     * @return
     */
    public Optional<Population> findPopulationById(Long populationId) {
        return populationRepository.findById(populationId);
    }

    /**
     * Find a population by studyId
     * @param studyId ID of the study
     * @return
     */
    public List<Population> findPopulationByStudyId(Long studyId) {
        return populationRepository.findByStudyId(studyId);
    }

    /**
     * Save a population
     * @param population population to be saved
     * @return
     */
    public Population savePopulation(Population population) {
        return populationRepository.save(population);
    }

    /**
     * Update a population
     * @param populationId ID of the population
     * @param updatedPopulation population to be updated
     * @return
     */
    public Optional<Population> updatePopulation(Long populationId, Population updatedPopulation) {
        Optional<Population> oldPopulation = populationRepository.findById(populationId);
        if (oldPopulation.isPresent()) {
            Population population = oldPopulation.get();
            population.setPopulationUrl(updatedPopulation.getPopulationUrl());
            population.setDescription(updatedPopulation.getDescription());
            population.setCharacteristics(updatedPopulation.getCharacteristics());
            population.setStudyId(updatedPopulation.getStudyId());
            Population savedPopulation = populationRepository.save(population);
            return Optional.of(savedPopulation);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a population
     * @param populationId ID of population to be deleted
     * @return
     */
    public boolean deletePopulation(Long populationId) {
        if(populationRepository.existsById(populationId)) {
            populationRepository.deleteById(populationId);
            return true;
        }else{
            return false;
        }
    }

    /**
     * Find a population by FeatureSetId
     * @param featureSetId ID of the featureSet
     * @return
     */
    public List<Population> getPopulationByFeatureSetId(Long featureSetId) {
        return this.populationRepository.findByFeatureSetId(featureSetId);
    }

}
