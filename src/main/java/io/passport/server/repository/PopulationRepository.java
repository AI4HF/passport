package io.passport.server.repository;

import io.passport.server.model.Population;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PopulationRepository extends JpaRepository<Population, Long> {
    Optional<Population> findByStudyId(Long studyId);
}
