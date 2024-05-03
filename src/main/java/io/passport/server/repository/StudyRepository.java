package io.passport.server.repository;
import io.passport.server.model.Study;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Study repository for database management.
 */
@Repository
public interface StudyRepository extends JpaRepository<Study, Long> {
    /**
     * Study id search method in case unique id is decided to be not enough.
     * @param studyId Non-unique study identifier set by users.
     * @return
     */
    Optional<Study> findByStudyId(String studyId);
    Page<Study> findAll(Pageable page);


}
