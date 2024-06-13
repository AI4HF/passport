package io.passport.server.service;

import io.passport.server.model.Personnel;
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

    @Autowired
    public PersonnelService(PersonnelRepository personnelRepository) {
        this.personnelRepository = personnelRepository;
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
    public Optional<Personnel> findPersonnelById(Long personnelId) {
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
     * @param personnel personnel to be saved
     * @return
     */
    public Personnel savePersonnel(Personnel personnel) {
        return personnelRepository.save(personnel);
    }

    /**
     * Update a personnel
     * @param personnelId ID of the personnel
     * @param updatedPersonnel personnel to be updated
     * @return
     */
    public Optional<Personnel> updatePersonnel(Long personnelId, Personnel updatedPersonnel) {
        Optional<Personnel> oldPersonnel = personnelRepository.findById(personnelId);
        if (oldPersonnel.isPresent()) {
            Personnel personnel = oldPersonnel.get();
            personnel.setFirstName(updatedPersonnel.getFirstName());
            personnel.setLastName(updatedPersonnel.getLastName());
            personnel.setEmail(updatedPersonnel.getEmail());
            personnel.setRole(updatedPersonnel.getRole());
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
    public boolean deletePersonnel(Long personnelId) {
        if(personnelRepository.existsById(personnelId)) {
            personnelRepository.deleteById(personnelId);
            return true;
        }else{
            return false;
        }
    }
}
