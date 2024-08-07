package io.passport.server.repository;

import io.passport.server.model.Passport;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Passport repository for database management.
 */
public interface PassportRepository extends JpaRepository<Passport, Long> {

}
