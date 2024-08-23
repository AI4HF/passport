package io.passport.server.repository;

import io.passport.server.model.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Parameter repository for database management.
 */
public interface ParameterRepository extends JpaRepository<Parameter, Long> {
    List<Parameter> findAllByStudyId(Long studyId);
}
