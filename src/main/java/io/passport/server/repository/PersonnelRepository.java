package io.passport.server.repository;

import io.passport.server.model.Personnel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Personnel repository for database management.
 */
@Repository
public interface PersonnelRepository extends JpaRepository<Personnel, Long> { }
