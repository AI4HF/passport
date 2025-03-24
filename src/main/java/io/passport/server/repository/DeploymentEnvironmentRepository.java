package io.passport.server.repository;


import io.passport.server.model.DeploymentEnvironment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DeploymentEnvironment repository for database management.
 */
public interface DeploymentEnvironmentRepository extends JpaRepository<DeploymentEnvironment, String> {
}
