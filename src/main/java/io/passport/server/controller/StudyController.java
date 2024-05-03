package io.passport.server.controller;

import io.passport.server.model.Study;
import io.passport.server.repository.StudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Class which stores the generated HTTP requests related to study operations.
 */
@RestController
@RequestMapping("/study")
public class StudyController {
    /**
     * Studyo repo access for database management.
     */
    private final StudyRepository studyRepository;

    @Autowired
    public StudyController(StudyRepository studyRepository) {
        this.studyRepository = studyRepository;
    }

    /**
     * Read all Studies 10 at a time.
     * Page counter is used to handle pagination.
     * @return
     */
    @GetMapping("/{page}")
    public ResponseEntity<List<Study>> getAllStudies(@PathVariable int page) {

            Pageable pageable = PageRequest.of(page, 10);
            Page<Study> studyPage = studyRepository.findAll(pageable);

            List<Study> studies = studyPage.getContent();
            return ResponseEntity.ok(studies);
    }

    /**
     * Create Study.
     * @param study Study model instance to be created.
     * @return
     */
    @PostMapping("/")
    public ResponseEntity<Study> createStudy(@RequestBody Study study) {
        Study savedStudy = studyRepository.save(study);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedStudy);
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
