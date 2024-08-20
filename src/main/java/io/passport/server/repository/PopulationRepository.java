package io.passport.server.repository;

import io.passport.server.model.Population;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Population repository for database management.
 */
@Repository
public interface PopulationRepository extends JpaRepository<Population, Long> {
    List<Population> findByStudyId(Long studyId);
}
