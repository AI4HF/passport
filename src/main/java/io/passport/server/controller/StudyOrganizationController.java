package io.passport.server.controller;

import io.passport.server.model.Organization;
import io.passport.server.model.Study;
import io.passport.server.model.StudyOrganization;
import io.passport.server.repository.OrganizationRepository;
import io.passport.server.repository.StudyRepository;
import io.passport.server.repository.StudyOrganizationRepository;
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
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/studyOrganization")
public class StudyOrganizationController {

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private StudyOrganizationRepository studyOrganizationRepository;

    @GetMapping("/organization/studies")
    public ResponseEntity<List<Study>> getStudiesByOrganization(
            @RequestParam Long organizationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<StudyOrganization> studyOrganizationPage = studyOrganizationRepository.findByOrganizationId(organizationId, pageable);

        List<Study> studyList = studyOrganizationPage.getContent().stream()
                .map(StudyOrganization::getStudy)
                .collect(Collectors.toList());
        long totalCount = studyOrganizationPage.getTotalElements();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(studyList);
    }

    @GetMapping("/study/organizations")
    public ResponseEntity<List<Organization>> getOrganizationsByStudy(
            @RequestParam Long studyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<StudyOrganization> studyOrganizationPage = studyOrganizationRepository.findByStudyId(studyId, pageable);

        List<Organization> organizationList = studyOrganizationPage.getContent().stream()
                .map(StudyOrganization::getOrganization)
                .collect(Collectors.toList());
        long totalCount = studyOrganizationPage.getTotalElements();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(organizationList);
    }

    /**
     * Update StudyOrganization.
     * @param studyId ID of the study that is to be updated.
     * @param organizationId ID of the organization that is to be updated.
     * @param studyOrganization StudyOrganization model instance with updated details.
     * @return
     */
    @PutMapping("/study/{studyId}/organization/{organizationId}")
    public ResponseEntity<StudyOrganization> updateStudyOrganization(
            @PathVariable Long studyId,
            @PathVariable Long organizationId,
            @RequestBody StudyOrganization studyOrganization) {

        Optional<StudyOrganization> optionalStudyOrganization = studyOrganizationRepository.findByStudyIdAndOrganizationId(studyId, organizationId);
        return optionalStudyOrganization.map(existingStudyOrganization -> {
            try {
                Utils.copyNonNullProperties(studyOrganization, existingStudyOrganization);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            StudyOrganization updatedStudyOrganization = studyOrganizationRepository.save(existingStudyOrganization);
            return ResponseEntity.ok(updatedStudyOrganization);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create StudyOrganization.
     * @param studyOrganization StudyOrganization model instance to be created.
     * @return
     */
    @PostMapping("/")
    public ResponseEntity<StudyOrganization> createStudyOrganization(@RequestBody StudyOrganization studyOrganization) {
        StudyOrganization savedStudyOrganization = studyOrganizationRepository.save(studyOrganization);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedStudyOrganization);
    }
}
