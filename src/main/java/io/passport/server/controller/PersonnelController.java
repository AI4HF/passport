package io.passport.server.controller;

import io.passport.server.model.Personnel;
import io.passport.server.repository.PersonnelRepository;
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
 * Class which stores the generated HTTP requests related to personnel operations.
 */
@RestController
@RequestMapping("/personnel")
public class PersonnelController {
    /**
     * Personnel repo access for database management.
     */
    private final PersonnelRepository personnelRepository;

    @Autowired
    public PersonnelController(PersonnelRepository personnelRepository) {
        this.personnelRepository = personnelRepository;
    }

    @GetMapping("/")
    public ResponseEntity<List<Personnel>> getAllPersonnelofOrganization(
            @RequestParam Long organizationId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "0") int pageSize) {

        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Personnel> personnelPage = personnelRepository.findByOrganizationId(organizationId, pageable);

        List<Personnel> personnelList = personnelPage.getContent();
        long totalCount = personnelPage.getTotalElements();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(personnelList);
    }

    @GetMapping("/{page}")
    public ResponseEntity<List<Personnel>> getAllPersonnel(@PathVariable int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Personnel> personnelPage = personnelRepository.findAll(pageable);

        List<Personnel> personnel = personnelPage.getContent();

        long totalCount = personnelRepository.count();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(personnel);
    }

    /**
     * Create Personnel.
     * @param personnel Personnel model instance to be created.
     * @return
     */
    @PostMapping("/")
    public ResponseEntity<Personnel> createPersonnel(@RequestBody Personnel personnel) {
        Personnel savedPersonnel = personnelRepository.save(personnel);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPersonnel);
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