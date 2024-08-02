package io.passport.server.controller;

import io.passport.server.model.Model;
import io.passport.server.service.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Class which stores the generated HTTP requests related to model operations.
 */
@RestController
@RequestMapping("/model")
public class ModelController {

    private static final Logger log = LoggerFactory.getLogger(ModelController.class);

    /**
     * Model service for model management.
     */
    private final ModelService modelService;

    @Autowired
    public ModelController(ModelService modelService) {
        this.modelService = modelService;
    }

    /**
     * Read models, if studyId is provided, filter by studyId; otherwise, return all models.
     * @param studyId Optional ID of the study.
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<Model>> getAllModels(@RequestParam(value = "studyId", required = false) Long studyId) {
        List<Model> models;

        if (studyId != null) {
            models = modelService.getAllModelsByStudyId(studyId);
        } else {
            models = modelService.getAllModels();
        }

        return ResponseEntity.ok(models);
    }

    /**
     * Read a model by id
     * @param modelId ID of the model
     * @return
     */
    @GetMapping("/{modelId}")
    public ResponseEntity<?> getModelById(@PathVariable Long modelId) {
        Optional<Model> model = this.modelService.findModelById(modelId);

        if (model.isPresent()) {
            return ResponseEntity.ok().body(model);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create a Model.
     * @param model model instance to be created.
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createModel(@RequestBody Model model) {
        try{
            Model savedModel = this.modelService.saveModel(model);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedModel);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Model.
     * @param modelId ID of the model that is to be updated.
     * @param updatedModel model instance with updated details.
     * @return
     */
    @PutMapping("/{modelId}")
    public ResponseEntity<?> updateModel(@PathVariable Long modelId, @RequestBody Model updatedModel) {
        try{
            Optional<Model> savedModel = this.modelService.updateModel(modelId, updatedModel);
            if (savedModel.isPresent()) {
                return ResponseEntity.ok().body(savedModel.get());
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete a model by Model ID.
     * @param modelId ID of the model that is to be deleted.
     * @return
     */
    @DeleteMapping("/{modelId}")
    public ResponseEntity<?> deleteModel(@PathVariable Long modelId) {
        try{
            boolean isDeleted = this.modelService.deleteModel(modelId);
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