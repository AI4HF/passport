package io.passport.server.controller;

import io.passport.server.model.Implementation;
import io.passport.server.service.ImplementationService;
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
 * Class which stores the generated HTTP requests related to implementation operations.
 */
@RestController
@RequestMapping("/implementation")
public class ImplementationController {
    private static final Logger log = LoggerFactory.getLogger(ImplementationController.class);
    /**
     * Implementation service for implementation management
     */
    private final ImplementationService implementationService;

    @Autowired
    public ImplementationController(ImplementationService implementationService) {
        this.implementationService = implementationService;
    }

    /**
     * Read all implementations
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<Implementation>> getAllImplementations() {
        List<Implementation> implementations = this.implementationService.getAllImplementations();

        long totalCount = implementations.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(implementations);
    }

    /**
     * Read an implementation by id
     * @param implementationId ID of the implementation
     * @return
     */
    @GetMapping("/{implementationId}")
    public ResponseEntity<?> getImplementation(@PathVariable Long implementationId) {
        Optional<Implementation> implementation = this.implementationService.findImplementationById(implementationId);

        if(implementation.isPresent()) {
            return ResponseEntity.ok().body(implementation.get());
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create Implementation.
     * @param implementation Implementation model instance to be created.
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createImplementation(@RequestBody Implementation implementation) {
        try{
            Implementation savedImplementation = this.implementationService.saveImplementation(implementation);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedImplementation);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Update Implementation.
     * @param implementationId ID of the implementation that is to be updated.
     * @param updatedImplementation Implementation model instance with updated details.
     * @return
     */
    @PutMapping("/{implementationId}")
    public ResponseEntity<?> updateImplementation(@PathVariable Long implementationId, @RequestBody Implementation updatedImplementation) {
        try{
            Optional<Implementation> savedImplementation = this.implementationService.updateImplementation(implementationId, updatedImplementation);
            if(savedImplementation.isPresent()) {
                return ResponseEntity.ok().body(savedImplementation);
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete by Implementation ID.
     * @param implementationId ID of the implementation that is to be deleted.
     * @return
     */
    @DeleteMapping("/{implementationId}")
    public ResponseEntity<?> deleteImplementation(@PathVariable Long implementationId) {
        try{
            boolean isDeleted = this.implementationService.deleteImplementation(implementationId);
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
