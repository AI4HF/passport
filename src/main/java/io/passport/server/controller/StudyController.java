package io.passport.server.controller;

import io.passport.server.model.Study;
import io.passport.server.repository.StudyRepository;
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
 * Class which stores the generated HTTP requests related to study operations.
 */
@RestController
@RequestMapping("/study")
public class StudyController {
    private static final Logger log = LoggerFactory.getLogger(StudyController.class);
    /**
     * Study repo access for database management.
     */
    private final StudyRepository studyRepository;

    @Autowired
    public StudyController(StudyRepository studyRepository) {
        this.studyRepository = studyRepository;
    }

    /**
     * Read all studies
     * @return
     */
    @GetMapping("/")
    public ResponseEntity<List<Study>> getAllStudies() {
        List<Study> studies = studyRepository.findAll();

        long totalCount = studyRepository.count(); // Fetch total count from the repository

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(studies);
    }

    /**
     * Read a study by id
     * @param studyId ID of the study
     * @return
     */
    @GetMapping("/{studyId}")
    public ResponseEntity<?> getStudy(@PathVariable Long studyId) {
        Optional<Study> study = studyRepository.findById(studyId);

        if(study.isPresent()) {
            return ResponseEntity.ok().body(study);
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create Study.
     * @param study Study model instance to be created.
     * @return
     */
    @PostMapping("/")
    public ResponseEntity<?> createStudy(@RequestBody Study study) {
        try{
            Study savedStudy = studyRepository.save(study);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedStudy);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Study.
     * @param studyId ID of the study that is to be updated.
     * @param updatedStudy Study model instance with updated details.
     * @return
     */
    @PutMapping("/{studyId}")
    public ResponseEntity<?> updateStudy(@PathVariable Long studyId, @RequestBody Study updatedStudy) {
        Optional<Study> optionalStudy = studyRepository.findById(studyId);
        if (optionalStudy.isPresent()) {
            Study study = optionalStudy.get();
            study.setName(updatedStudy.getName());
            study.setDescription(updatedStudy.getDescription());
            study.setObjectives(updatedStudy.getObjectives());
            study.setEthics(updatedStudy.getEthics());
            study.setOwner(updatedStudy.getOwner());
            try{
                Study savedStudy = studyRepository.save(study);
                return ResponseEntity.ok(savedStudy);
            }catch (Exception e){
             log.error(e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete by Study ID.
     * @param studyId ID of the study that is to be deleted.
     * @return
     */
    @DeleteMapping("/{studyId}")
    public ResponseEntity<Object> deleteStudy(@PathVariable Long studyId) {
        return studyRepository.findById(studyId)
                .map(study -> {
                    studyRepository.delete(study);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
