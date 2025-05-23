package io.passport.server.repository;
import io.passport.server.model.Study;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Study repository for database management.
 */
@Repository
public interface StudyRepository extends JpaRepository<Study, String> {

    // Join with population table and get related Study for the dataset
    @Query("SELECT new Study(s.id, s.name, s.description, s.objectives, s.ethics, s.owner)  " +
            "FROM Dataset d, Study s, Population p WHERE d.populationId = p.populationId AND p.studyId = s.id AND d.datasetId = :datasetId")
    Study findByDatasetId(@Param("datasetId") String datasetId);

    List<Study> findByOwner(String owner);
}
