package io.passport.server.repository;

import io.passport.server.model.ModelDeployment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


/**
 * ModelDeployment repository for database management.
 */
public interface ModelDeploymentRepository extends JpaRepository<ModelDeployment, Long> {
    Optional<ModelDeployment> findByEnvironmentId(Long environmentId);
}
