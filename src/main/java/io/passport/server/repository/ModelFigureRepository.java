package io.passport.server.repository;

import io.passport.server.model.ModelFigure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ModelFigure repository for database management.
 */
@Repository
public interface ModelFigureRepository extends JpaRepository<ModelFigure, String> {
    List<ModelFigure> findByModelId(String modelId);
}
