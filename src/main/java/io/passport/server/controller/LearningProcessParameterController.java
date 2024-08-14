package io.passport.server.controller;

import io.passport.server.model.LearningProcessParameterDTO;
import io.passport.server.model.LearningProcessParameter;
import io.passport.server.model.LearningProcessParameterId;
import io.passport.server.service.LearningProcessParameterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class which stores the generated HTTP requests related to LearningProcessParameter operations.
 */
@RestController
@RequestMapping("/learning-process-parameter")
public class LearningProcessParameterController {
    private static final Logger log = LoggerFactory.getLogger(LearningProcessParameterController.class);

    /**
     * LearningProcessParameter service for LearningProcessParameter management
     */
    private final LearningProcessParameterService learningProcessParameterService;

    @Autowired
    public LearningProcessParameterController(LearningProcessParameterService learningProcessParameterService) {
        this.learningProcessParameterService = learningProcessParameterService;
    }

    /**
     * Read all LearningProcessParameters or filtered by learningProcessId and/or parameterId
     * @param learningProcessId ID of the LearningProcess (optional)
     * @param parameterId ID of the Parameter (optional)
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<LearningProcessParameterDTO>> getLearningProcessParameters(
            @RequestParam(required = false) Long learningProcessId,
            @RequestParam(required = false) Long parameterId) {

        List<LearningProcessParameter> parameters;

        if (learningProcessId != null && parameterId != null) {
            LearningProcessParameterId id = new LearningProcessParameterId();
            id.setLearningProcessId(learningProcessId);
            id.setParameterId(parameterId);
            Optional<LearningProcessParameter> parameter = this.learningProcessParameterService.findLearningProcessParameterById(id);
            parameters = parameter.map(List::of).orElseGet(List::of);
        } else if (learningProcessId != null) {
            parameters = this.learningProcessParameterService.findByLearningProcessId(learningProcessId);
        } else if (parameterId != null) {
            parameters = this.learningProcessParameterService.findByParameterId(parameterId);
        } else {
            parameters = this.learningProcessParameterService.getAllLearningProcessParameters();
        }

        List<LearningProcessParameterDTO> dtos = parameters.stream()
                .map(entity -> new LearningProcessParameterDTO(entity))
                .collect(Collectors.toList());

        long totalCount = dtos.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(dtos);
    }

    /**
     * Create a new LearningProcessParameter entity.
     * @param learningProcessParameterDTO the DTO containing data for the new LearningProcessParameter
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createLearningProcessParameter(@RequestBody LearningProcessParameterDTO learningProcessParameterDTO) {
        try {
            LearningProcessParameter learningProcessParameter = new LearningProcessParameter(learningProcessParameterDTO);
            LearningProcessParameter savedLearningProcessParameter = this.learningProcessParameterService.saveLearningProcessParameter(learningProcessParameter);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedLearningProcessParameter);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update LearningProcessParameter using query parameters.
     * @param learningProcessId ID of the LearningProcess
     * @param parameterId ID of the Parameter
     * @param updatedLearningProcessParameter LearningProcessParameter model instance with updated details.
     * @return
     */
    @PutMapping()
    public ResponseEntity<?> updateLearningProcessParameter(
            @RequestParam Long learningProcessId,
            @RequestParam Long parameterId,
            @RequestBody LearningProcessParameter updatedLearningProcessParameter) {

        LearningProcessParameterId learningProcessParameterId = new LearningProcessParameterId();
        learningProcessParameterId.setLearningProcessId(learningProcessId);
        learningProcessParameterId.setParameterId(parameterId);

        try {
            Optional<LearningProcessParameter> savedLearningProcessParameter = this.learningProcessParameterService.updateLearningProcessParameter(learningProcessParameterId, updatedLearningProcessParameter);
            if (savedLearningProcessParameter.isPresent()) {
                return ResponseEntity.ok().body(savedLearningProcessParameter);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by LearningProcessParameter composite ID using query parameters.
     * @param learningProcessId ID of the LearningProcess
     * @param parameterId ID of the Parameter
     * @return
     */
    @DeleteMapping()
    public ResponseEntity<?> deleteLearningProcessParameter(
            @RequestParam Long learningProcessId,
            @RequestParam Long parameterId) {

        LearningProcessParameterId learningProcessParameterId = new LearningProcessParameterId();
        learningProcessParameterId.setLearningProcessId(learningProcessId);
        learningProcessParameterId.setParameterId(parameterId);

        try {
            boolean isDeleted = this.learningProcessParameterService.deleteLearningProcessParameter(learningProcessParameterId);
            if (isDeleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

