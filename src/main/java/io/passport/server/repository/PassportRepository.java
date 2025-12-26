package io.passport.server.repository;

import io.passport.server.model.Passport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Passport repository for database management.
 */
@Repository
public interface PassportRepository extends JpaRepository<Passport, String> {
    List<Passport> findAllByStudyId(String studyId);
    List<Passport> findByDeploymentId(String deploymentId);
    // Find Passports modified by a specific Personnel
    @Query("SELECT p FROM Passport p WHERE p.createdBy = :personnelId OR p.approvedBy = :personnelId")
    List<Passport> findByCreatedByOrApprovedBy(@Param("personnelId") String personnelId);

    // Find Study ID directly from Passport ID
    @Query("SELECT p.studyId FROM Passport p WHERE p.passportId = :passportId")
    Optional<String> findStudyIdByPassportId(@Param("passportId") String passportId);

}
