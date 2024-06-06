package io.passport.server.repository;

import io.passport.server.model.Personnel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Personnel repository for database management.
 */
@Repository
public interface PersonnelRepository extends JpaRepository<Personnel, Long> {

    Page<Personnel> findByOrganizationId(Long organizationId, Pageable pageable);
    Page<Personnel> findAll(Pageable page);
}
