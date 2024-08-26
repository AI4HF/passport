package io.passport.server.repository;

import io.passport.server.model.Passport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Passport repository for database management.
 */
public interface PassportRepository extends JpaRepository<Passport, Long> {
    List<Passport> findAllByStudyId(Long studyId);
}
