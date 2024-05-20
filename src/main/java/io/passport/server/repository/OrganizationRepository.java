package io.passport.server.repository;

import io.passport.server.model.Organization;
import io.passport.server.model.Study;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Study repository for database management.
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Page<Organization> findAll(Pageable page);
}