package io.passport.server.service;

import io.passport.server.model.Implementation;
import io.passport.server.repository.ImplementationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for implementation management.
 */
@Service
public class ImplementationService {

    /**
     * Implementation repo access for database management.
     */
    private final ImplementationRepository implementationRepository;

    @Autowired
    public ImplementationService(ImplementationRepository implementationRepository) {
        this.implementationRepository = implementationRepository;
    }

    /**
     * Return all implementations
     * @return
     */
    public List<Implementation> getAllImplementations() {
        return implementationRepository.findAll();
    }

    /**
     * Find an implementation by implementationId
     * @param implementationId ID of the implementation
     * @return
     */
    public Optional<Implementation> findImplementationById(Long implementationId) {
        return implementationRepository.findById(implementationId);
    }

    /**
     * Save an implementation
     * @param implementation implementation to be saved
     * @return
     */
    public Implementation saveImplementation(Implementation implementation) {
        return implementationRepository.save(implementation);
    }

    /**
     * Update an implementation
     * @param implementationId ID of the implementation
     * @param updatedImplementation implementation to be updated
     * @return
     */
    public Optional<Implementation> updateImplementation(Long implementationId, Implementation updatedImplementation) {
        Optional<Implementation> oldImplementation = implementationRepository.findById(implementationId);
        if (oldImplementation.isPresent()) {
            Implementation implementation = oldImplementation.get();
            implementation.setAlgorithmId(updatedImplementation.getAlgorithmId());
            implementation.setSoftware(updatedImplementation.getSoftware());
            implementation.setName(updatedImplementation.getName());
            implementation.setDescription(updatedImplementation.getDescription());
            Implementation savedImplementation = implementationRepository.save(implementation);
            return Optional.of(savedImplementation);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete an implementation
     * @param implementationId ID of implementation to be deleted
     * @return
     */
    public Optional<Implementation> deleteImplementation(Long implementationId) {
        Optional<Implementation> existingImplementation = implementationRepository.findById(implementationId);
        if (existingImplementation.isPresent()) {
            implementationRepository.delete(existingImplementation.get());
            return existingImplementation;
        } else {
            return Optional.empty();
        }
    }

}
