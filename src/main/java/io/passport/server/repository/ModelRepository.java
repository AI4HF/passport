package io.passport.server.repository;

import io.passport.server.model.Model;
import io.passport.server.model.ModelDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Model repository for database management.
 */
public interface ModelRepository extends JpaRepository<Model, Long> {
    List<Model> findByStudyId(Long studyId);

    /** Custom query to retrieve a list of ModelDto objects, which include details about models and associated deploymentId. */
    @Query("SELECT new io.passport.server.model.ModelDto(" +
            "md.deploymentId, m.modelId, m.learningProcessId, m.studyId, m.name, m.version, m.tag, m.modelType, " +
            "m.productIdentifier, m.owner, m.trlLevel, m.license, m.primaryUse, m.secondaryUse, m.intendedUsers, " +
            "m.counterIndications, m.ethicalConsiderations, m.limitations, m.fairnessConstraints, m.createdAt, " +
            "m.createdBy, m.lastUpdatedAt, m.lastUpdatedBy) " +
            "FROM Model m JOIN ModelDeployment md ON m.modelId = md.modelId")
    List<ModelDto> findAllModelsInDeployments();

    /** Custom query to retrieve a list of ModelDto objects, which include details about models and associated passportId. */
    @Query("SELECT new io.passport.server.model.ModelDto(" +
            "p.passportId, m.modelId, m.learningProcessId, m.studyId, m.name, m.version, m.tag, m.modelType, " +
            "m.productIdentifier, m.owner, m.trlLevel, m.license, m.primaryUse, m.secondaryUse, m.intendedUsers, " +
            "m.counterIndications, m.ethicalConsiderations, m.limitations, m.fairnessConstraints, m.createdAt, " +
            "m.createdBy, m.lastUpdatedAt, m.lastUpdatedBy) " +
            "FROM Model m " +
            "JOIN ModelDeployment md ON m.modelId = md.modelId " +
            "JOIN Passport p ON p.deploymentId = md.deploymentId")
    List<ModelDto> findAllModelsInPassports();
}
