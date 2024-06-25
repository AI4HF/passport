package io.passport.server.repository;


import io.passport.server.model.Survey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Survey repository for database management.
 */
public interface SurveyRepository extends JpaRepository<Survey, Long> {
    List<Survey> findAllByStudyId(Long StudyId);
}
