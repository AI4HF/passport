package io.passport.server.repository;

import io.passport.server.model.StudyOrganization;
import io.passport.server.model.composite_keys.StudyOrganizationKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyOrganizationRepository extends JpaRepository<StudyOrganization, StudyOrganizationKey> {
    Page<StudyOrganization> findByOrganizationId(Long organizationId, Pageable pageable);
    Page<StudyOrganization> findByStudyId(Long studyId, Pageable pageable);
}