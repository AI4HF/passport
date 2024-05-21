package io.passport.server.repository;


import io.passport.server.model.StudyPersonnel;
import io.passport.server.model.composite_keys.StudyPersonnelKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyPersonnelRepository extends JpaRepository<StudyPersonnel, StudyPersonnelKey> {
    Page<StudyPersonnel> findByPersonnelId(Long personnelId, Pageable pageable);
    Page<StudyPersonnel> findByStudyId(Long studyId, Pageable pageable);
}