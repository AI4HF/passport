package io.passport.server.service;

import io.passport.server.model.LinkedArticle;
import io.passport.server.repository.LinkedArticleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Linked Article management.
 */
@Service
public class LinkedArticleService {

    @Autowired
    private LinkedArticleRepository linkedArticleRepository;

    /**
     * Create new LinkedArticle entries for a study.
     * @param studyId study ID to associate
     * @param articles list of articles
     * @return saved entities
     */
    @Transactional
    public List<LinkedArticle> createLinkedArticleEntries(String studyId, List<LinkedArticle> articles) {
        List<LinkedArticle> toSave = articles.stream().map(a -> {
            LinkedArticle na = new LinkedArticle();
            na.setLinkedArticleId(a.getLinkedArticleId());
            na.setStudyId(studyId);
            na.setArticleUrl(a.getArticleUrl());
            return na;
        }).collect(Collectors.toList());

        return linkedArticleRepository.saveAll(toSave);
    }

    /**
     * Read all LinkedArticles by study.
     */
    public List<LinkedArticle> findByStudyId(String studyId) {
        return linkedArticleRepository.findByStudyId(studyId);
    }
}
