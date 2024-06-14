package io.passport.server.controller;

import io.passport.server.model.Personnel;
import io.passport.server.service.StudyPersonnelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Class which stores the generated HTTP requests related to StudyPersonnel operations.
 */
@RestController
@RequestMapping("/studyPersonnel")
public class StudyPersonnelController {

    private static final Logger log = LoggerFactory.getLogger(StudyPersonnelController.class);

    /**
     * StudyPersonnel service for studyPersonnel management
     */
    private final StudyPersonnelService studyPersonnelService;

    @Autowired
    public StudyPersonnelController(StudyPersonnelService studyPersonnelService) {
        this.studyPersonnelService = studyPersonnelService;
    }

    /**
     * Get all personnel related to a study.
     * @param studyId ID of the study.
     * @return
     */
    @GetMapping("/personnel")
    public ResponseEntity<?> getPersonnelByStudyId(@RequestParam Long studyId) {
        try{
            List<Personnel> personnel = this.studyPersonnelService.findPersonnelByStudyId(studyId);
            return ResponseEntity.ok(personnel);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Clear all old StudyPersonnel entries related to the study and create new ones. Return updated personnel list.
     * @param studyId ID of the study.
     * @return
     */
    @PostMapping("/personnel")
    public ResponseEntity<?> createStudyPersonnelEntries(@RequestParam Long studyId, @RequestBody List<Personnel> personnel) {
        try{
            this.studyPersonnelService.createStudyPersonnelEntries(studyId, personnel);
            List<Personnel> updatedPersonnel = this.studyPersonnelService.findPersonnelByStudyId(studyId);
            return ResponseEntity.ok(updatedPersonnel);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
