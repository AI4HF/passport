package io.passport.server.repository;

import io.passport.server.model.Implementation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Implementation repository for database management.
 */
@Repository
public interface ImplementationRepository extends JpaRepository<Implementation, String> {
    List<Implementation> findByAlgorithmId(String algorithmId);
}
