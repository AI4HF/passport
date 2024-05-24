package io.passport.server.controller;

import io.passport.server.model.Personnel;
import io.passport.server.repository.PersonnelRepository;
import io.passport.server.service.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

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
    public ResponseEntity<List<Personnel>> getAllPersonnel(
            @RequestParam Long organizationId,
            @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, 10);
        Page<Personnel> personnelPage = personnelRepository.findByOrganizationId(organizationId, pageable);

        List<Personnel> personnelList = personnelPage.getContent();
        long totalCount = personnelPage.getTotalElements();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(personnelList);
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
     * @param personnelId ID of the personnel that is to be updated.
     * @param personnel Personnel model instance with updated details.
     * @return
     */
    @PutMapping("/{personnelId}")
    public ResponseEntity<Personnel> updatePersonnel(
            @PathVariable Long personnelId,
            @RequestBody Personnel personnel) {
        return personnelRepository.findById(personnelId)
                .map(existingPersonnel -> {
                    try {
                        Utils.copyNonNullProperties(personnel, existingPersonnel);
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    Personnel updatedPersonnel = personnelRepository.save(existingPersonnel);
                    return ResponseEntity.ok(updatedPersonnel);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
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