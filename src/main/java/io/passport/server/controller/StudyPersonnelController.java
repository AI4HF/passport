package io.passport.server.controller;

import io.passport.server.model.Personnel;
import io.passport.server.model.Study;
import io.passport.server.model.StudyPersonnel;
import io.passport.server.repository.PersonnelRepository;
import io.passport.server.repository.StudyRepository;
import io.passport.server.repository.StudyPersonnelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/studyPersonnel")
public class StudyPersonnelController {

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private PersonnelRepository personnelRepository;

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
}

