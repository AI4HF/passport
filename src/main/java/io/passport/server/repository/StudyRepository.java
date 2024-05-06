package io.passport.server.repository;
import io.passport.server.model.Study;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Study repository for database management.
 */
@Repository
public interface StudyRepository extends JpaRepository<Study, Long> {
    /**
     * Repository generated method for finding all the studies with the given page parameters.
     * @param page Pageable parameter which sets the required amount of data from the database.
     */
    Page<Study> findAll(Pageable page);
}
