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
public interface ModelDeploymentRepository extends JpaRepository<ModelDeployment, String> {
    Optional<ModelDeployment> findByEnvironmentId(String environmentId);

    // Used for cascading from Model
    List<ModelDeployment> findByModelId(String modelId);

    // Find Deployments modified by a specific Personnel
    @Query("SELECT md FROM ModelDeployment md WHERE md.createdBy = :personnelId OR md.lastUpdatedBy = :personnelId")
    List<ModelDeployment> findByCreatedByOrLastUpdatedBy(@Param("personnelId") String personnelId);

    // Find Study ID directly from Deployment ID
    @Query("SELECT m.studyId FROM ModelDeployment md JOIN Model m ON md.modelId = m.modelId WHERE md.deploymentId = :deploymentId")
    Optional<String> findStudyIdByDeploymentId(@Param("deploymentId") String deploymentId);

    // Join with model table and get related modelDeployments for the study
    @Query("SELECT new ModelDeployment(md.deploymentId, md.modelId, md.environmentId, md.tags, md.identifiedFailures, md.status, md.createdAt, md.createdBy, md.lastUpdatedAt, md.lastUpdatedBy)  " +
            "FROM Model m, ModelDeployment md WHERE md.modelId = m.modelId AND m.studyId = :studyId")
    List<ModelDeployment> findAllByStudyId(@Param("studyId")String studyId);
}
