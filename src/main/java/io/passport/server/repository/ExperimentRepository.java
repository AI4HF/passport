package io.passport.server.repository;

import io.passport.server.model.Experiment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExperimentRepository extends JpaRepository<Experiment, Long> {
    Page<Experiment> findByStudyId(Long studyId, Pageable pageable);
}