package io.passport.server.repository;

import io.passport.server.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;

/**
 * StudyPersonnel repository for database management.
 */
@Repository
public interface StudyPersonnelRepository extends JpaRepository<StudyPersonnel, StudyPersonnelId> {
    // Join with personnel table and get related personnel for the study
    @Query("SELECT new Personnel(p.personId, p.organizationId, p.firstName, p.lastName, p.email)  " +
            "FROM Personnel p, StudyPersonnel sp WHERE sp.id.personnelId = p.personId AND sp.id.studyId = :studyId AND p.organizationId = :organizationId")
    List<Personnel> findPersonnelByStudyIdAndOrganizationId(@Param("studyId") String studyId, @Param("organizationId") String organizationId);

    // Delete StudyPersonnel entries before inserting new ones
    @Modifying
    @Transactional
    @Query("DELETE FROM StudyPersonnel sp WHERE sp.id.studyId = :studyId AND sp.id.personnelId IN :personnelIdList")
    void deleteByStudyIdAndPersonnelId(@Param("studyId") String studyId, @Param("personnelIdList") List<String> personnelIdList);

    // Join with study table and get related studies for the personnel
    @Query("SELECT new Study(s.id, s.name, s.description, s.objectives, s.ethics, s.owner)  " +
            "FROM Study s, StudyPersonnel sp WHERE sp.id.studyId = s.id AND sp.id.personnelId = :personnelId")
    List<Study> findStudiesByPersonnelId(@Param("personnelId") String personnelId);

    @Query("SELECT sp FROM StudyPersonnel sp WHERE sp.id.studyId = :studyId AND sp.id.personnelId IN :personnelIdList")
    List<StudyPersonnel> findByStudyIdAndPersonnelIdList(@Param("studyId") String studyId, @Param("personnelIdList") List<String> personnelIdList);

    List<StudyPersonnel> findStudyPersonnelById_PersonnelId(String personId);

    @Query("SELECT sp " +
            "FROM StudyPersonnel sp " +
            "JOIN Personnel p ON sp.id.personnelId = p.personId " +
            "WHERE sp.id.studyId = :studyId AND p.organizationId = :organizationId")
    List<StudyPersonnel> findStudyPersonnelByStudyIdAndOrganizationId(@Param("studyId") String studyId,
                                                                      @Param("organizationId") String organizationId);

}