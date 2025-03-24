package io.passport.server.repository;

import io.passport.server.model.Model;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Model repository for database management.
 */
public interface ModelRepository extends JpaRepository<Model, String> {
    List<Model> findByStudyId(String studyId);
}
