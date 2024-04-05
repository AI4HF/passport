package io.passport.server.repository;

import io.passport.server.model.Study;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyRepository extends JpaRepository<Study, Long> {
    Optional<Study> findByStudyId(String studyId);


}
