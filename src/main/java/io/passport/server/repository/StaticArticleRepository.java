package io.passport.server.repository;

import io.passport.server.model.StaticArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * StaticArticle repository for database management.
 */
public interface StaticArticleRepository extends JpaRepository<StaticArticle, String> {

    List<StaticArticle> findByStudyId(String studyId);

    void deleteAllByStudyId(String studyId);

    // Join with studyPersonnel to fetch related Static Articles for a given personnel
    @Query("""
           SELECT new StaticArticle(a.staticArticleId, a.studyId, a.articleUrl)
             FROM StudyPersonnel sp, StaticArticle a
            WHERE sp.id.studyId = a.studyId
              AND sp.id.personnelId = :personnelId
           """)
    List<StaticArticle> findStaticArticlesByPersonnelId(@Param("personnelId") String personnelId);
}
