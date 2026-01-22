package io.passport.server.repository;

import io.passport.server.model.LearningProcessParameter;
import io.passport.server.model.LearningProcessParameterId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * LearningProcessParameter repository for database management.
 */
@Repository
public interface LearningProcessParameterRepository extends JpaRepository<LearningProcessParameter, LearningProcessParameterId> {
    List<LearningProcessParameter> findByIdLearningProcessId(String learningProcessId);
    List<LearningProcessParameter> findByIdParameterId(String parameterId);

    // Find LearningProcessParameters by study id
    @Query("SELECT lpp FROM LearningProcessParameter lpp, Parameter p WHERE " +
            "lpp.id.parameterId = p.parameterId AND p.studyId = :studyId")
    List<LearningProcessParameter> findByStudyId(@Param("studyId") String studyId);
}
