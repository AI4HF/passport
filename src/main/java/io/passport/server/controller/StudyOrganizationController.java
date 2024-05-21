package io.passport.server.controller;

import io.passport.server.model.Organization;
import io.passport.server.model.Study;
import io.passport.server.model.StudyOrganization;
import io.passport.server.repository.OrganizationRepository;
import io.passport.server.repository.StudyRepository;
import io.passport.server.repository.StudyOrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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
}
