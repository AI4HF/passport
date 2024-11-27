package io.passport.server.service;

import io.passport.server.model.Personnel;
import io.passport.server.model.PersonnelDTO;
import io.passport.server.model.Role;
import io.passport.server.repository.PersonnelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for personnel management.
 */
@Service
public class PersonnelService {

    /**
     * Personnel repo access for database management.
     */
    private final PersonnelRepository personnelRepository;

    /**
     * Keycloak service for keycloak user management.
     */
    private final KeycloakService keycloakService;

    @Autowired
    public PersonnelService(PersonnelRepository personnelRepository, KeycloakService keycloakService) {
        this.personnelRepository = personnelRepository;
        this.keycloakService = keycloakService;
    }

    /**
     * Get all personnel
     */
    public List<Personnel> getAllPersonnel() {
        return personnelRepository.findAll();
    }

    /**
     * Find a personnel by personnelId
     * @param personnelId ID of the personnel
     * @return
     */
    public Optional<Personnel> findPersonnelById(String personnelId) {
        return personnelRepository.findById(personnelId);
    }

    /**
     * Find personnel by organizationId
     * @param organizationId ID of the organization
     * @return
     */
    public List<Personnel> findPersonnelByOrganizationId(Long organizationId) {
        return personnelRepository.findByOrganizationId(organizationId);
    }

    /**
     * Save a personnel
     * @param personnelDTO personnel to be saved
     * @return
     */
    public Optional<Personnel> savePersonnel(PersonnelDTO personnelDTO) {
        Optional<String> keycloakUserId = this.keycloakService
                .createUserAndReturnId(personnelDTO.getCredentials().username, personnelDTO.getCredentials().password, Role.valueOf(personnelDTO.getPersonnel().getRole()));  // No role assignment
        if(keycloakUserId.isPresent()) {
            Personnel personnel = personnelDTO.getPersonnel();
            personnel.setPersonId(keycloakUserId.get());
            Personnel savedPersonnel = personnelRepository.save(personnel);
            return Optional.of(savedPersonnel);
        }else{
            return Optional.empty();
        }
    }

    /**
     * Update a personnel
     * @param personnelId ID of the personnel
     * @param updatedPersonnel personnel to be updated
     * @return
     */
    public Optional<Personnel> updatePersonnel(String personnelId, Personnel updatedPersonnel) {
        Optional<Personnel> oldPersonnel = personnelRepository.findById(personnelId);
        if (oldPersonnel.isPresent()) {
            keycloakService.updateRole(personnelId, Role.valueOf(updatedPersonnel.getRole()));
            Personnel personnel = oldPersonnel.get();
            personnel.setFirstName(updatedPersonnel.getFirstName());
            personnel.setLastName(updatedPersonnel.getLastName());
            personnel.setEmail(updatedPersonnel.getEmail());
            personnel.setOrganizationId(updatedPersonnel.getOrganizationId());
            Personnel savedPersonnel = personnelRepository.save(personnel);
            return Optional.of(savedPersonnel);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a personnel
     * @param personnelId ID of personnel to be deleted
     * @return
     */
    public boolean deletePersonnel(String personnelId) {
        if(personnelRepository.existsById(personnelId)) {
            boolean isKeycloakUserDeleted = keycloakService.deleteUser(personnelId);
            if(isKeycloakUserDeleted) {
                personnelRepository.deleteById(personnelId);
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }
}
