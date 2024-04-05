package io.passport.server.controller;

import io.passport.server.model.Study;
import io.passport.server.repository.StudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/study")
public class StudyController {
    private final StudyRepository studyRepository;

    @Autowired
    public StudyController(StudyRepository studyRepository) {
        this.studyRepository = studyRepository;
    }

    /**
     * Read all Studies.
     * @return
     */
    @GetMapping("/")
    public ResponseEntity<List<Study>> getAllStudies() {
        List<Study> studies = studyRepository.findAll();
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
    public ResponseEntity<Object> deleteStudy(@PathVariable String studyId) {
        return studyRepository.findByStudyId(studyId)
                .map(study -> {
                    studyRepository.delete(study);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
