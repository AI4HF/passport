package io.passport.server.repository;

import io.passport.server.model.Population;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PopulationRepository extends JpaRepository<Population, Long> {
    Page<Population> findByStudyId(Long studyId, Pageable pageable);
}
