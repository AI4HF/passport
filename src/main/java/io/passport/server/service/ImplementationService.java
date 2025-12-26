package io.passport.server.service;

import io.passport.server.model.Implementation;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.ImplementationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    private final RoleCheckerService roleCheckerService;

    /**
     * Lazy service references for limited use in cascade validation
     */
    @Autowired @Lazy private LearningProcessService learningProcessService;

    @Autowired
    public ImplementationService(ImplementationRepository implementationRepository,
                                 RoleCheckerService roleCheckerService) {
        this.implementationRepository = implementationRepository;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Starts a validation chain of Implementations and all of their children for cascades
     *
     * @param studyId Id of the Study
     * @param implementationId Id of the Implementation
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateImplementationDeletion(String studyId, String implementationId, Jwt principal) {
        List<ValidationResult> results = new ArrayList<>();

        results.add(learningProcessService.validateCascade(studyId, "Implementation", implementationId, principal));

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
        List<Implementation> affectedImplementations;

        switch (sourceResourceType) {
            case "Algorithm":
                affectedImplementations = implementationRepository.findByAlgorithmId(sourceResourceId);
                break;
            default:
                return new ValidationResult(1, "");
        }

        if (affectedImplementations.isEmpty()) {
            return new ValidationResult(1, "");
        }

        List<ValidationResult> childResults = new ArrayList<>();
        boolean authorized = true;

        for (Implementation impl : affectedImplementations) {
            boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                    studyId,
                    principal,
                    List.of(Role.DATA_SCIENTIST)
            );

            if (!hasPermission) {
                authorized = false;
                break;
            }

            childResults.add(validateImplementationDeletion(studyId, impl.getImplementationId(), principal));
        }

        if (!authorized) {
            return new ValidationResult(0, "Implementation");
        }

        childResults.add(new ValidationResult(1, "Implementation"));

        return ValidationResult.aggregate(childResults);
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
    public Optional<Implementation> findImplementationById(String implementationId) {
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
    public Optional<Implementation> updateImplementation(String implementationId, Implementation updatedImplementation) {
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
    public Optional<Implementation> deleteImplementation(String implementationId) {
        Optional<Implementation> existingImplementation = implementationRepository.findById(implementationId);
        if (existingImplementation.isPresent()) {
            implementationRepository.delete(existingImplementation.get());
            return existingImplementation;
        } else {
            return Optional.empty();
        }
    }

}
