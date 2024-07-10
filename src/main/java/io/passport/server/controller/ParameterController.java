package io.passport.server.controller;

import io.passport.server.model.Parameter;
import io.passport.server.service.ParameterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Class which stores the generated HTTP requests related to parameter. operations.
 */
@RestController
@RequestMapping("/parameter")
public class ParameterController {

    private static final Logger log = LoggerFactory.getLogger(ParameterController.class);

    /**
     * Parameter service for parameter management.
     */
    private final ParameterService parameterService;

    @Autowired
    public ParameterController(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    /**
     * Read all parameters
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<Parameter>> getAllParameters() {

        List<Parameter> parameters = this.parameterService.getAllParameters();

        long totalCount = parameters.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(parameters);
    }

    /**
     * Read a parameter by id
     * @param parameterId ID of the parameter
     * @return
     */
    @GetMapping("/{parameterId}")
    public ResponseEntity<?> getParameterById(@PathVariable Long parameterId) {
        Optional<Parameter> parameter = this.parameterService.findParameterById(parameterId);

        if (parameter.isPresent()) {
            return ResponseEntity.ok().body(parameter);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * Create a Parameter.
     * @param parameter parameter model instance to be created.
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createParameter(@RequestBody Parameter parameter) {
        try{
            Parameter savedParameter = this.parameterService.saveParameter(parameter);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedParameter);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Parameter.
     * @param parameterId ID of the parameter that is to be updated.
     * @param updatedParameter model instance with updated details.
     * @return
     */
    @PutMapping("/{parameterId}")
    public ResponseEntity<?> updateParameter(@PathVariable Long parameterId, @RequestBody Parameter updatedParameter) {
        try{
            Optional<Parameter> savedParameter = this.parameterService.updateParameter(parameterId, updatedParameter);
            if (savedParameter.isPresent()) {
                return ResponseEntity.ok().body(savedParameter.get());
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete a parameter by Parameter ID.
     * @param parameterId ID of the parameter that is to be deleted.
     * @return
     */
    @DeleteMapping("/{parameterId}")
    public ResponseEntity<?> deleteParameter(@PathVariable Long parameterId) {
        try{
            boolean isDeleted = this.parameterService.deleteParameter(parameterId);
            if(isDeleted) {
                return ResponseEntity.noContent().build();
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
