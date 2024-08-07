package io.passport.server.controller;

import io.passport.server.model.Passport;
import io.passport.server.service.PassportService;
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
 * Class which stores the generated HTTP requests related to passport operations.
 */
@RestController
@RequestMapping("/passport")
public class PassportController {

    private static final Logger log = LoggerFactory.getLogger(PassportController.class);
    /**
     * Passport service for passport management
     */
    private final PassportService passportService;

    @Autowired
    public PassportController(PassportService passportService) {
        this.passportService = passportService;
    }

    /**
     * Read all passports
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<Passport>> getAllPassports() {
        List<Passport> passports = this.passportService.getAllPassports();

        long totalCount = passports.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(passports);
    }

    /**
     * Read a passport by passportId
     * @param passportId ID of the passport
     * @return
     */
    @GetMapping("/{passportId}")
    public ResponseEntity<?> getPassport(@PathVariable Long passportId) {
        Optional<Passport> passport = this.passportService.findPassportByPassportId(passportId);

        if(passport.isPresent()) {
            return ResponseEntity.ok().body(passport.get());
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create Passport.
     * @param passport Passport model instance to be created.
     * @return
     */
    @PostMapping()
    public ResponseEntity<?> createPassport(@RequestBody Passport passport) {
        try{
            Passport savedPassport = this.passportService.savePassport(passport);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPassport);
        }catch(Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Delete passport by passportID.
     * @param passportId ID of the passport that is to be deleted.
     * @return
     */
    @DeleteMapping("/{passportId}")
    public ResponseEntity<?> deletePassport(@PathVariable Long passportId) {
        try{
            boolean isDeleted = this.passportService.deletePassport(passportId);
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
