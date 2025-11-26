package io.passport.server.service;

import io.passport.server.model.LinkedArticle;
import io.passport.server.repository.LinkedArticleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for Linked Article management.
 */
@Service
public class LinkedArticleService {

    @Autowired
    private LinkedArticleRepository linkedArticleRepository;

    /**
     * Overwrite all Linked Article entries for a Study
     * @param studyId ID of the study
     * @param articles Collection of Articles to be overwritten as
     * @return Final state of overwritten Articles
     */
    @Transactional
    public List<LinkedArticle> replaceLinkedArticles(String studyId, List<LinkedArticle> articles) {
        if (articles.isEmpty()) {
            linkedArticleRepository.deleteAllByStudyId(studyId);
            return Collections.emptyList();
        }

        Set<String> incomingIds = articles.stream()
                .map(LinkedArticle::getLinkedArticleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        linkedArticleRepository.deleteByStudyIdAndLinkedArticleIdNotIn(studyId, incomingIds);

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
