package io.passport.server.service;

import io.passport.server.model.Passport;
import io.passport.server.repository.PassportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service class for passport management.
 */
@Service
public class PassportService {

    /**
     * Passport repo access for database management.
     */
    private final PassportRepository passportRepository;


    @Autowired
    public PassportService(PassportRepository passportRepository) {
        this.passportRepository = passportRepository;
    }

    /**
     * Return all passports
     * @return
     */
    public List<Passport> getAllPassports() {
        return passportRepository.findAll();
    }


    /**
     * Find a passport by passportId
     * @param passportId ID of the passport
     * @return
     */
    public Optional<Passport> findPassportByPassportId(Long passportId) {
        return passportRepository.findById(passportId);
    }


    /**
     * Save a passport
     * @param passport passport to be saved
     * @return
     */
    public Passport savePassport(Passport passport) {
        // Set the creation and approval time
        Instant now = Instant.now();
        passport.setCreatedAt(now);
        passport.setApprovedAt(now);

        return passportRepository.save(passport);
    }



    /**
     * Delete a passport
     * @param passportId ID of passport to be deleted
     * @return
     */
    public boolean deletePassport(Long passportId) {
        if(passportRepository.existsById(passportId)) {
            passportRepository.deleteById(passportId);
            return true;
        }else{
            return false;
        }
    }
}
