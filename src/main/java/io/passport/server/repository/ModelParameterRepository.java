package io.passport.server.repository;

import io.passport.server.model.ModelParameter;
import io.passport.server.model.ModelParameterId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ModelParameter repository for database management.
 */
@Repository
public interface ModelParameterRepository extends JpaRepository<ModelParameter, ModelParameterId> {
    List<ModelParameter> findByIdModelId(String modelId);
    List<ModelParameter> findByIdParameterId(String parameterId);
}
