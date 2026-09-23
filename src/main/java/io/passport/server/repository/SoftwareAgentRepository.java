package io.passport.server.repository;

import io.passport.server.model.SoftwareAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * SoftwareAgent repository for database management.
 */
@Repository
public interface SoftwareAgentRepository extends JpaRepository<SoftwareAgent, String> {

    Optional<SoftwareAgent> findByKeycloakClientId(String keycloakClientId);
}
