package io.passport.server.controller;

import io.passport.server.model.Study;
import io.passport.server.repository.StudyRepository;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/study")
public class StudyController {
    private final StudyRepository studyRepository;

    @Autowired
    public StudyController(StudyRepository studyRepository) {
        this.studyRepository = studyRepository;
    }

    @GetMapping("/")
    public ResponseEntity<List<Study>> getAllStudies() {
        List<Study> studies = studyRepository.findAll();
        return ResponseEntity.ok(studies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Study> getStudyById(@PathVariable Long id) {
        Optional<Study> optionalStudy = studyRepository.findById(id);
        return optionalStudy.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/")
    public ResponseEntity<Study> createStudy(@RequestBody Study study, Authentication authentication) {
        if (isAuthorized(authentication)) {
            Study savedStudy = studyRepository.save(study);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedStudy);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Study> updateStudy(@PathVariable Long id, @RequestBody Study updatedStudy, Authentication authentication) {
        if (isAuthorized(authentication)) {
            Optional<Study> optionalStudy = studyRepository.findById(id);
            if (optionalStudy.isPresent()) {
                updatedStudy.setId(id);
                Study savedStudy = studyRepository.save(updatedStudy);
                return ResponseEntity.ok(savedStudy);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudy(@PathVariable Long id, Authentication authentication) {
        if (isAuthorized(authentication)) {
            Optional<Study> optionalStudy = studyRepository.findById(id);
            if (optionalStudy.isPresent()) {
                studyRepository.deleteById(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    private boolean isAuthorized(Authentication authentication) {
        if (authentication instanceof KeycloakAuthenticationToken) {
            KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) authentication;
            return token.getAccount().getRoles().contains("YOUR_REQUIRED_ROLE");
        }
        return false;
    }
}
