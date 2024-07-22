package io.passport.server.controller;

import io.passport.server.model.ModelDeployment;
import io.passport.server.service.ModelDeploymentService;
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
 * Class which stores the generated HTTP requests related to model deployment operations.
 */
@RestController
@RequestMapping("/modelDeployment")
public class ModelDeploymentController {


    private static final Logger log = LoggerFactory.getLogger(ModelDeploymentController.class);
    /**
     * ModelDeployment service for deployment management
     */
    private final ModelDeploymentService modelDeploymentService;

    @Autowired
    public ModelDeploymentController(ModelDeploymentService modelDeploymentService) {
        this.modelDeploymentService = modelDeploymentService;
    }


    /**
     * Read all model deployments
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<ModelDeployment>> getAllModelDeployments() {
        List<ModelDeployment> modelDeployments = this.modelDeploymentService.getAllModelDeployments();

        long totalCount = modelDeployments.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(modelDeployments);
    }


    /**
     * Read a model deployment by deploymentId
     * @param deploymentId ID of the model deployment
     * @return
     */
    @GetMapping("/{deploymentId}")
    public ResponseEntity<?> getModelDeployment(@PathVariable Long deploymentId) {
        Optional<ModelDeployment> modelDeployment = this.modelDeploymentService.findModelDeploymentByDeploymentId(deploymentId);

        if(modelDeployment.isPresent()) {
            return ResponseEntity.ok().body(modelDeployment.get());
        }else{
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * Create ModelDeployment.
     * @param modelDeployment ModelDeployment model instance to be created.
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createModelDeployment(@RequestBody ModelDeployment modelDeployment) {
        try{
            ModelDeployment savedModelDeployment = this.modelDeploymentService.saveModelDeployment(modelDeployment);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedModelDeployment);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    /**
     * Update ModelDeployment.
     * @param deploymentId ID of the model deployment that is to be updated.
     * @param updatedModelDeployment ModelDeployment model instance with updated details.
     * @return
     */
    @PutMapping("/{deploymentId}")
    public ResponseEntity<?> updateModelDeployment(@PathVariable Long deploymentId, @RequestBody ModelDeployment updatedModelDeployment) {
        try{
            Optional<ModelDeployment> savedModelDeployment = this.modelDeploymentService.updateModelDeployment(deploymentId, updatedModelDeployment);
            if(savedModelDeployment.isPresent()) {
                return ResponseEntity.ok().body(savedModelDeployment);
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    /**
     * Delete by deployment ID.
     * @param deploymentId ID of the model deployment that is to be deleted.
     * @return
     */
    @DeleteMapping("/{deploymentId}")
    public ResponseEntity<?> deleteModelDeployment(@PathVariable Long deploymentId) {
        try{
            boolean isDeleted = this.modelDeploymentService.deleteModelDeployment(deploymentId);
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
