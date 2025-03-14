package io.passport.server.repository;

import io.passport.server.model.Passport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Passport repository for database management.
 */
@Repository
public interface PassportRepository extends JpaRepository<Passport, String> {
    List<Passport> findAllByStudyId(String studyId);
}
