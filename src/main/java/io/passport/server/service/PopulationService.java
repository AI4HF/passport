package io.passport.server.service;

import io.passport.server.model.Population;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.PopulationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    private final RoleCheckerService roleCheckerService;

    /**
     * Lazy service references for limited use in cascade validation
     */
    @Autowired @Lazy private DatasetService datasetService;
    @Autowired @Lazy private StudyOrganizationService studyOrganizationService;

    @Autowired
    public PopulationService(PopulationRepository populationRepository,
                             RoleCheckerService roleCheckerService) {
        this.populationRepository = populationRepository;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Starts a validation chain of Population and all of their children for cascades
     *
     * @param studyId Id of the Study
     * @param populationId Id of the Population
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validatePopulationDeletion(String studyId, String populationId, Jwt principal) {
        List<ValidationResult> results = new ArrayList<>();

        results.add(datasetService.validateCascade(studyId, "Population", populationId, principal));
        results.add(studyOrganizationService.validateCascade(studyId, "Population", populationId, principal));

        return ValidationResult.aggregate(results);
    }

    /**
     * Determines which entities are to be cascaded based on the request from the previous element in the chain
     * Continues the chain by directing to the next entries through the other validation method
     *
     * @param studyId Id of the Study
     * @param sourceResourceType Resource type of the parent element in the Cascade chain
     * @param sourceResourceId Resource id of the parent element in the Cascade chain
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateCascade(String studyId, String sourceResourceType, String sourceResourceId, Jwt principal) {
        List<Population> affectedPopulations;

        switch (sourceResourceType) {
            case "Study":
                affectedPopulations = populationRepository.findByStudyId(sourceResourceId);
                break;
            default:
                return new ValidationResult(true, "");
        }

        if (affectedPopulations.isEmpty()) {
            return new ValidationResult(true, "");
        }

        List<ValidationResult> childResults = new ArrayList<>();
        boolean authorized = true;

        for (Population pop : affectedPopulations) {
            boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                    studyId,
                    principal,
                    List.of(Role.STUDY_OWNER)
            );

            if (!hasPermission) {
                authorized = false;
                break;
            }

            childResults.add(validatePopulationDeletion(studyId, pop.getPopulationId(), principal));
        }

        if (!authorized) {
            return new ValidationResult(false, "Population");
        }

        childResults.add(new ValidationResult(true, "Population"));

        return ValidationResult.aggregate(childResults);
    }

    public List<Population> findAllPopulations() {
        return populationRepository.findAll();
    }

    /**
     * Find a population by populationId
     * @param populationId ID of the population
     * @return
     */
    public Optional<Population> findPopulationById(String populationId) {
        return populationRepository.findById(populationId);
    }

    /**
     * Find a population by studyId
     * @param studyId ID of the study
     * @return
     */
    public List<Population> findPopulationByStudyId(String studyId) {
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
    public Optional<Population> updatePopulation(String populationId, Population updatedPopulation) {
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
    public Optional<Population> deletePopulation(String populationId) {
        Optional<Population> existingPopulation = populationRepository.findById(populationId);
        if (existingPopulation.isPresent()) {
            populationRepository.delete(existingPopulation.get());
            return existingPopulation;
        } else {
            return Optional.empty();
        }
    }


    /**
     * Find a population by FeatureSetId
     * @param featureSetId ID of the featureSet
     * @return
     */
    public List<Population> getPopulationByFeatureSetId(String featureSetId) {
        return this.populationRepository.findByFeatureSetId(featureSetId);
    }

}
