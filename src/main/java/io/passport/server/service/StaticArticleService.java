package io.passport.server.service;

import io.passport.server.model.StaticArticle;
import io.passport.server.repository.StaticArticleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Static Article management.
 */
@Service
public class StaticArticleService {

    @Autowired
    private StaticArticleRepository staticArticleRepository;

    /**
     * Create new StaticArticle entries for a study.
     * @param studyId study ID to associate
     * @param articles list of articles
     * @return saved entities
     */
    @Transactional
    public List<StaticArticle> createStaticArticleEntries(String studyId, List<StaticArticle> articles) {
        List<StaticArticle> toSave = articles.stream().map(a -> {
            StaticArticle na = new StaticArticle();
            na.setStaticArticleId(a.getStaticArticleId());
            na.setStudyId(studyId);
            na.setArticleUrl(a.getArticleUrl());
            return na;
        }).collect(Collectors.toList());

        return staticArticleRepository.saveAll(toSave);
    }

    /**
     * Read all StaticArticles by study.
     */
    public List<StaticArticle> findByStudyId(String studyId) {
        return staticArticleRepository.findByStudyId(studyId);
    }
}
