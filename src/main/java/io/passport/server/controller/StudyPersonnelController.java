package io.passport.server.controller;

import io.passport.server.model.Personnel;
import io.passport.server.model.Study;
import io.passport.server.model.StudyPersonnel;
import io.passport.server.model.composite_keys.StudyPersonnelKey;
import io.passport.server.repository.PersonnelRepository;
import io.passport.server.repository.StudyRepository;
import io.passport.server.repository.StudyPersonnelRepository;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/studyPersonnel")
public class StudyPersonnelController {

    @Autowired
    private StudyPersonnelRepository studyPersonnelRepository;

    @GetMapping("/personnel/studies")
    public ResponseEntity<List<Study>> getStudiesByPersonnel(
            @RequestParam Long personnelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<StudyPersonnel> studyPersonnelPage = studyPersonnelRepository.findByPersonnelId(personnelId, pageable);

        List<Study> studyList = studyPersonnelPage.getContent().stream()
                .map(StudyPersonnel::getStudy)
                .collect(Collectors.toList());
        long totalCount = studyPersonnelPage.getTotalElements();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(studyList);
    }

    @GetMapping("/study/personnel")
    public ResponseEntity<List<Personnel>> getPersonnelByStudy(
            @RequestParam Long studyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<StudyPersonnel> studyPersonnelPage = studyPersonnelRepository.findByStudyId(studyId, pageable);

        List<Personnel> personnelList = studyPersonnelPage.getContent().stream()
                .map(StudyPersonnel::getPersonnel)
                .collect(Collectors.toList());
        long totalCount = studyPersonnelPage.getTotalElements();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(personnelList);
    }

    /**
     * Update StudyPersonnel.
     * @param studyId ID of the study that is to be updated.
     * @param personnelId ID of the personnel that is to be updated.
     * @param updatedStudyPersonnel StudyPersonnel model instance with updated details.
     * @return
     */
    @PutMapping("/{studyId}/{personnelId}")
    public ResponseEntity<StudyPersonnel> updateStudyPersonnel(@PathVariable Long studyId, @PathVariable Long personnelId, @RequestBody StudyPersonnel updatedStudyPersonnel) {
        StudyPersonnelKey key = new StudyPersonnelKey(studyId, personnelId);
        Optional<StudyPersonnel> optionalStudyPersonnel = studyPersonnelRepository.findById(key);
        if (optionalStudyPersonnel.isPresent()) {
            StudyPersonnel studyPersonnel = optionalStudyPersonnel.get();
            studyPersonnel.setRole(updatedStudyPersonnel.getRole());

            StudyPersonnel savedStudyPersonnel = studyPersonnelRepository.save(studyPersonnel);
            return ResponseEntity.ok(savedStudyPersonnel);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create StudyPersonnel.
     * @param studyPersonnel StudyPersonnel model instance to be created.
     * @return
     */
    @PostMapping("/")
    public ResponseEntity<StudyPersonnel> createStudyPersonnel(@RequestBody StudyPersonnel studyPersonnel) {
        StudyPersonnel savedStudyPersonnel = studyPersonnelRepository.save(studyPersonnel);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedStudyPersonnel);
    }
}

