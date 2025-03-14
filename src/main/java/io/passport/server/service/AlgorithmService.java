package io.passport.server.service;

import io.passport.server.model.Algorithm;
import io.passport.server.repository.AlgorithmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for algorithm management.
 */
@Service
public class AlgorithmService {

    /**
     * Algorithm repo access for database management.
     */
    private final AlgorithmRepository algorithmRepository;

    @Autowired
    public AlgorithmService(AlgorithmRepository algorithmRepository) {
        this.algorithmRepository = algorithmRepository;
    }

    /**
     * Return all algorithms
     * @return
     */
    public List<Algorithm> getAllAlgorithms() {
        return algorithmRepository.findAll();
    }

    /**
     * Find an algorithm by algorithmId
     * @param algorithmId ID of the algorithm
     * @return
     */
    public Optional<Algorithm> findAlgorithmById(String algorithmId) {
        return algorithmRepository.findById(algorithmId);
    }

    /**
     * Save an algorithm
     * @param algorithm algorithm to be saved
     * @return
     */
    public Algorithm saveAlgorithm(Algorithm algorithm) {
        return algorithmRepository.save(algorithm);
    }

    /**
     * Update an algorithm
     * @param algorithmId ID of the algorithm
     * @param updatedAlgorithm algorithm to be updated
     * @return
     */
    public Optional<Algorithm> updateAlgorithm(String algorithmId, Algorithm updatedAlgorithm) {
        Optional<Algorithm> oldAlgorithm = algorithmRepository.findById(algorithmId);
        if (oldAlgorithm.isPresent()) {
            Algorithm algorithm = oldAlgorithm.get();
            algorithm.setName(updatedAlgorithm.getName());
            algorithm.setObjectiveFunction(updatedAlgorithm.getObjectiveFunction());
            algorithm.setType(updatedAlgorithm.getType());
            algorithm.setSubType(updatedAlgorithm.getSubType());
            Algorithm savedAlgorithm = algorithmRepository.save(algorithm);
            return Optional.of(savedAlgorithm);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete an algorithm and return the deleted entity.
     * @param algorithmId ID of the algorithm to be deleted.
     * @return Optional containing the deleted algorithm if found, otherwise empty.
     */
    public Optional<Algorithm> deleteAlgorithm(String algorithmId) {
        Optional<Algorithm> existingAlgorithm = algorithmRepository.findById(algorithmId);
        if (existingAlgorithm.isPresent()) {
            algorithmRepository.delete(existingAlgorithm.get());
            return existingAlgorithm;
        }else{
            return Optional.empty();
        }
    }
}
