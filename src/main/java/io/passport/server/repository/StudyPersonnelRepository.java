package io.passport.server.repository;

import io.passport.server.model.Personnel;
import io.passport.server.model.StudyPersonnel;
import io.passport.server.model.StudyPersonnelId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

/**
 * StudyPersonnel repository for database management.
 */
@Repository
public interface StudyPersonnelRepository extends JpaRepository<StudyPersonnel, StudyPersonnelId> {
    // Join with personnel table and get related personnel for the study
    @Query("SELECT p FROM Personnel p, StudyPersonnel sp WHERE sp.id.personnelId = p.id AND sp.id.studyId = :studyId")
    List<Personnel> findPersonnelByStudyId(@Param("studyId") Long studyId);

    // Delete StudyPersonnel entries before inserting new ones
    @Modifying
    @Transactional
    @Query("DELETE FROM StudyPersonnel sp WHERE sp.id.studyId = :studyId")
    void deleteByStudyId(@Param("studyId") Long studyId);
}