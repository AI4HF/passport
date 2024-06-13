package io.passport.server.controller;

import io.passport.server.model.Personnel;
import io.passport.server.service.PersonnelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
    private final PersonnelService personnelService;

    @Autowired
    public PersonnelController(PersonnelService personnelService) {
        this.personnelService = personnelService;
    }

    /**
     * If organizationId exists, get all personnel related to this organizationId otherwise get all personnel.
     * @param organizationId ID of the organization related to personnel.
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<Personnel>> getPersonnelByOrganizationId(@RequestParam Optional<Long> organizationId) {

        List<Personnel> personnel = organizationId
                .map(this.personnelService::findPersonnelByOrganizationId)
                .orElseGet(this.personnelService::getAllPersonnel);


        long totalCount = personnel.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(personnel);
    }

    /**
     * Read personnel by personId
     * @param personId ID of the personnel.
     * @return
     */
    @GetMapping("/{personId}")
    public ResponseEntity<?> getPersonnelByPersonId(
            @PathVariable("personId") Long personId) {

        Optional<Personnel> personnel = this.personnelService.findPersonnelById(personId);

        if(personnel.isPresent()) {
            return ResponseEntity.ok().body(personnel);
        }else{
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * Create a Personnel.
     * @param personnel Personnel model instance to be created.
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createPersonnel(@RequestBody Personnel personnel) {
        try{
            Personnel savedPersonnel = this.personnelService.savePersonnel(personnel);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPersonnel);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Personnel.
     * @param personId ID of the personnel that is to be updated.
     * @param updatedPersonnel Personnel model instance with updated details.
     * @return
     */
    @PutMapping("/{personId}")
    public ResponseEntity<?> updatePersonnel(@PathVariable Long personId, @RequestBody Personnel updatedPersonnel) {
        try{
            Optional<Personnel> savedPersonnel = this.personnelService.updatePersonnel(personId, updatedPersonnel);
            if(savedPersonnel.isPresent()) {
                return ResponseEntity.ok().body(savedPersonnel.get());
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete a personnel by Personnel ID.
     * @param personId ID of the personnel that is to be deleted.
     * @return
     */
    @DeleteMapping("/{personId}")
    public ResponseEntity<?> deletePersonnel(@PathVariable Long personId) {
        try{
            boolean isDeleted = this.personnelService.deletePersonnel(personId);
            if(isDeleted) {
                return ResponseEntity.noContent().build();
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}