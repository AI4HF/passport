package io.passport.server.repository;

import io.passport.server.model.Survey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
    Page<Survey> findByStudyId(Long studyId, Pageable pageable);
}
