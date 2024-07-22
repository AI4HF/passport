package io.passport.server.repository;

import io.passport.server.model.ModelDeployment;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * ModelDeployment repository for database management.
 */
public interface ModelDeploymentRepository extends JpaRepository<ModelDeployment, Long> {

}
