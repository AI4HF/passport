package io.passport.server.repository;

import io.passport.server.model.Algorithm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Algorithm repository for database management.
 */
@Repository
public interface AlgorithmRepository extends JpaRepository<Algorithm, Long> {
}
