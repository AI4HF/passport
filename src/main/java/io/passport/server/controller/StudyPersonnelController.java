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
     * Get all personnel related to a study and an organization.
     * @param studyId ID of the study.
     * @param organizationId ID of the organization
     * @return
     */
    @GetMapping("/personnel")
    public ResponseEntity<?> getPersonnelByStudyId(@RequestParam Long studyId, @RequestParam Long organizationId) {
        try{
            List<Personnel> personnel = this.studyPersonnelService.findPersonnelByStudyIdAndOrganizationId(studyId, organizationId);
            return ResponseEntity.ok(personnel);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Clear all old StudyPersonnel entries related to both the study and the organization then create new ones. Return updated personnel list.
     * @param studyId ID of the study.
     * @param organizationId ID of the organization.
     * @param personnel List of personnel to be used in StudyPersonnel entries
     * @return
     */
    @PostMapping("/personnel")
    public ResponseEntity<?> createStudyPersonnelEntries(@RequestParam Long studyId, @RequestParam Long organizationId, @RequestBody List<Personnel> personnel) {
        try{
            this.studyPersonnelService.createStudyPersonnelEntries(studyId, organizationId, personnel);
            List<Personnel> updatedPersonnel = this.studyPersonnelService.findPersonnelByStudyIdAndOrganizationId(studyId, organizationId);
            return ResponseEntity.ok(updatedPersonnel);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
