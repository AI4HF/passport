package io.passport.server.repository;

import io.passport.server.model.DatasetTransformationStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * DatasetTransformationStep repository for database management.
 */
@Repository
public interface DatasetTransformationStepRepository extends JpaRepository<DatasetTransformationStep, Long> {
    List<DatasetTransformationStep> findByDataTransformationId(Long dataTransformationId);
}

