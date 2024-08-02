package io.passport.server.repository;

import io.passport.server.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Organization repository for database management.
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    List<Organization> findByOrganizationAdminId(String organizationAdminId);
}