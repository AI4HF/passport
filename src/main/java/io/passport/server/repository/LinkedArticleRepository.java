package io.passport.server.repository;

import io.passport.server.model.LinkedArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * LinkedArticle repository for database management.
 */
public interface LinkedArticleRepository extends JpaRepository<LinkedArticle, String> {
    List<LinkedArticle> findByStudyId(String studyId);

    void deleteAllByStudyId(String studyId);

    void deleteByStudyIdAndLinkedArticleIdNotIn(String studyId, Collection<String> linkedArticleIds);
}
