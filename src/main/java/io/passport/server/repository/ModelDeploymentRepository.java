package io.passport.server.repository;

import io.passport.server.model.ModelDeployment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


/**
 * ModelDeployment repository for database management.
 */
public interface ModelDeploymentRepository extends JpaRepository<ModelDeployment, Long> {
    Optional<ModelDeployment> findByEnvironmentId(Long environmentId);

    // Join with model table and get related modelDeployments for the study
    @Query("SELECT new ModelDeployment(md.deploymentId, md.modelId, md.environmentId, md.tags, md.identifiedFailures, md.status, md.createdAt, md.createdBy, md.lastUpdatedAt, md.lastUpdatedBy)  " +
            "FROM Model m, ModelDeployment md WHERE md.modelId = m.modelId AND m.studyId = :studyId")
    List<ModelDeployment> findAllByStudyId(@Param("studyId")Long studyId);
}
