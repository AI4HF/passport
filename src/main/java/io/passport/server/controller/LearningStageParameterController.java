package io.passport.server.controller;

import io.passport.server.model.LearningStageParameterDTO;
import io.passport.server.model.LearningStageParameter;
import io.passport.server.model.LearningStageParameterId;
import io.passport.server.service.LearningStageParameterService;
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
 * Class which stores the generated HTTP requests related to LearningStageParameter operations.
 */
@RestController
@RequestMapping("/learning-stage-parameter")
public class LearningStageParameterController {
    private static final Logger log = LoggerFactory.getLogger(LearningStageParameterController.class);

    /**
     * LearningStageParameter service for LearningStageParameter management
     */
    private final LearningStageParameterService learningStageParameterService;

    @Autowired
    public LearningStageParameterController(LearningStageParameterService learningStageParameterService) {
        this.learningStageParameterService = learningStageParameterService;
    }

    /**
     * Read all LearningStageParameters or filtered by learningStageId and/or parameterId
     * @param learningStageId ID of the LearningStage (optional)
     * @param parameterId ID of the Parameter (optional)
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<LearningStageParameterDTO>> getLearningStageParameters(
            @RequestParam(required = false) Long learningStageId,
            @RequestParam(required = false) Long parameterId) {

        List<LearningStageParameter> parameters;

        if (learningStageId != null && parameterId != null) {
            LearningStageParameterId id = new LearningStageParameterId();
            id.setLearningStageId(learningStageId);
            id.setParameterId(parameterId);
            Optional<LearningStageParameter> parameter = this.learningStageParameterService.findLearningStageParameterById(id);
            parameters = parameter.map(List::of).orElseGet(List::of);
        } else if (learningStageId != null) {
            parameters = this.learningStageParameterService.findByLearningStageId(learningStageId);
        } else if (parameterId != null) {
            parameters = this.learningStageParameterService.findByParameterId(parameterId);
        } else {
            parameters = this.learningStageParameterService.getAllLearningStageParameters();
        }

        List<LearningStageParameterDTO> dtos = parameters.stream()
                .map(entity -> new LearningStageParameterDTO(entity))
                .collect(Collectors.toList());

        long totalCount = dtos.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(dtos);
    }

    /**
     * Create a new LearningStageParameter entity.
     * @param learningStageParameterDTO the DTO containing data for the new LearningStageParameter
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createLearningStageParameter(@RequestBody LearningStageParameterDTO learningStageParameterDTO) {
        try {
            LearningStageParameter learningStageParameter = new LearningStageParameter(learningStageParameterDTO);
            LearningStageParameter savedLearningStageParameter = this.learningStageParameterService.saveLearningStageParameter(learningStageParameter);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedLearningStageParameter);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update LearningStageParameter using query parameters.
     * @param learningStageId ID of the LearningStage
     * @param parameterId ID of the Parameter
     * @param updatedLearningStageParameter LearningStageParameter model instance with updated details.
     * @return
     */
    @PutMapping()
    public ResponseEntity<?> updateLearningStageParameter(
            @RequestParam Long learningStageId,
            @RequestParam Long parameterId,
            @RequestBody LearningStageParameter updatedLearningStageParameter) {

        LearningStageParameterId learningStageParameterId = new LearningStageParameterId();
        learningStageParameterId.setLearningStageId(learningStageId);
        learningStageParameterId.setParameterId(parameterId);

        try {
            Optional<LearningStageParameter> savedLearningStageParameter = this.learningStageParameterService.updateLearningStageParameter(learningStageParameterId, updatedLearningStageParameter);
            if (savedLearningStageParameter.isPresent()) {
                return ResponseEntity.ok().body(savedLearningStageParameter);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by LearningStageParameter composite ID using query parameters.
     * @param learningStageId ID of the LearningStage
     * @param parameterId ID of the Parameter
     * @return
     */
    @DeleteMapping()
    public ResponseEntity<?> deleteLearningStageParameter(
            @RequestParam Long learningStageId,
            @RequestParam Long parameterId) {

        LearningStageParameterId learningStageParameterId = new LearningStageParameterId();
        learningStageParameterId.setLearningStageId(learningStageId);
        learningStageParameterId.setParameterId(parameterId);

        try {
            boolean isDeleted = this.learningStageParameterService.deleteLearningStageParameter(learningStageParameterId);
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

