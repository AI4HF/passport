package io.passport.server.controller;

import io.passport.server.model.Personnel;
import io.passport.server.repository.PersonnelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Class which stores the generated HTTP requests related to personnel operations.
 */
@RestController
@RequestMapping("/personnel")
public class PersonnelController {

    private static final Logger log = LoggerFactory.getLogger(PersonnelController.class);

    /**
     * Personnel repo access for database management.
     */
    private final PersonnelRepository personnelRepository;

    @Autowired
    public PersonnelController(PersonnelRepository personnelRepository) {
        this.personnelRepository = personnelRepository;
    }

    /**
     * Read personnel by organizationId
     * @param organizationId ID of the organization related to personnel.
     * @return
     */
    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<List<Personnel>> getPersonnelByOrganizationId(
            @PathVariable("organizationId") Long organizationId) {

        List<Personnel> personnel = personnelRepository.findByOrganizationId(organizationId);

        return ResponseEntity.ok().body(personnel);
    }

    /**
     * Read personnel by personId
     * @param personId ID of the personnel.
     * @return
     */
    @GetMapping("/{personId}")
    public ResponseEntity<?> getPersonnelByPersonId(
            @PathVariable("personId") Long personId) {

        Optional<Personnel> personnel = personnelRepository.findById(personId);

        if(personnel.isPresent()) {
            return ResponseEntity.ok().body(personnel);
        }else{
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * Create Personnel.
     * @param personnel Personnel model instance to be created.
     * @return
     */
    @PostMapping("/")
    public ResponseEntity<?> createPersonnel(@RequestBody Personnel personnel) {
        try{
            Personnel savedPersonnel = personnelRepository.save(personnel);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPersonnel);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Personnel.
     * @param id ID of the personnel that is to be updated.
     * @param updatedPersonnel Personnel model instance with updated details.
     * @return
     */
    @PutMapping("/{id}")
    public ResponseEntity<Personnel> updatePersonnel(@PathVariable Long id, @RequestBody Personnel updatedPersonnel) {
        Optional<Personnel> optionalPersonnel = personnelRepository.findById(id);
        if (optionalPersonnel.isPresent()) {
            Personnel personnel = optionalPersonnel.get();
            personnel.setFirstName(updatedPersonnel.getFirstName());
            personnel.setLastName(updatedPersonnel.getLastName());
            personnel.setRole(updatedPersonnel.getRole());
            personnel.setEmail(updatedPersonnel.getEmail());
            personnel.setOrganizationId(updatedPersonnel.getOrganizationId());
            Personnel savedPersonnel = personnelRepository.save(personnel);
            return ResponseEntity.ok(savedPersonnel);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete by Personnel ID.
     * @param personnelId ID of the personnel that is to be deleted.
     * @return
     */
    @DeleteMapping("/{personnelId}")
    public ResponseEntity<Object> deletePersonnel(@PathVariable Long personnelId) {
        return personnelRepository.findById(personnelId)
                .map(personnel -> {
                    personnelRepository.delete(personnel);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}