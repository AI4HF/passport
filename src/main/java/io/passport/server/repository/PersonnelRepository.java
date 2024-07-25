package io.passport.server.repository;

import io.passport.server.model.Personnel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Personnel repository for database management.
 */
@Repository
public interface PersonnelRepository extends JpaRepository<Personnel, String> {

    List<Personnel> findByOrganizationId(Long organizationId);
}
