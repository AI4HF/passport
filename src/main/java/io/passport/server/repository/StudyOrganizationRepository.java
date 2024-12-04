package io.passport.server.repository;

import io.passport.server.model.Organization;
import io.passport.server.model.Study;
import io.passport.server.model.StudyOrganization;
import io.passport.server.model.StudyOrganizationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


/**
 * StudyOrganization repository for database management.
 */
public interface StudyOrganizationRepository extends JpaRepository<StudyOrganization, StudyOrganizationId> {

    // Join with organization table and get related organizations for the study
    @Query("SELECT new Organization(o.organizationId, o.name, o.address, o.organizationAdminId)  " +
            "FROM Organization o, StudyOrganization so WHERE so.id.organizationId = o.organizationId AND so.id.studyId = :studyId")
    List<Organization> findOrganizationsByStudyId(@Param("studyId") Long studyId);

    // Join with study table and get related organizations for the organization
    @Query("SELECT new Study(s.id, s.name, s.description, s.objectives, s.ethics, s.owner)  " +
            "FROM Study s, StudyOrganization so WHERE so.id.studyId = s.id AND so.id.organizationId = :organizationId")
    List<Study> findStudiesByOrganizationId(@Param("organizationId") Long organizationId);
}
