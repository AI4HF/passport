package io.passport.server.service;

import io.passport.server.model.LinkedArticle;
import io.passport.server.model.Role;
import io.passport.server.model.ValidationResult;
import io.passport.server.repository.LinkedArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for Linked Article management.
 */
@Service
public class LinkedArticleService {

    /**
     * LinkedArticle repo access for database management.
     */
    private final LinkedArticleRepository linkedArticleRepository;
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public LinkedArticleService(LinkedArticleRepository linkedArticleRepository,
                                RoleCheckerService roleCheckerService) {
        this.linkedArticleRepository = linkedArticleRepository;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Determines which entities are to be cascaded based on the request from the previous element in the chain
     * Continues the chain by directing to the next entries through the other validation method
     *
     * @param studyId Id of the Study
     * @param sourceResourceType Resource type of the parent element in the Cascade chain
     * @param sourceResourceId Resource id of the parent element in the Cascade chain
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateCascade(String studyId, String sourceResourceType, String sourceResourceId, Jwt principal) {
        List<LinkedArticle> affectedArticles;

        switch (sourceResourceType) {
            case "Study":
                affectedArticles = linkedArticleRepository.findByStudyId(sourceResourceId);
                break;
            default:
                return new ValidationResult(1, "");
        }

        if (affectedArticles.isEmpty()) {
            return new ValidationResult(1, "");
        }

        boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                studyId,
                principal,
                List.of(Role.STUDY_OWNER)
        );

        if (!hasPermission) {
            return new ValidationResult(0, "LinkedArticle");
        }

        return new ValidationResult(1, "LinkedArticle");
    }
    public List<LinkedArticle> findAllLinkedArticles() {
        return linkedArticleRepository.findAll();
    }

    /**
     * Find a linked article by linkedArticleId
     * @param linkedArticleId ID of the linked article
     * @return
     */
    public Optional<LinkedArticle> findLinkedArticleById(String linkedArticleId) {
        return linkedArticleRepository.findById(linkedArticleId);
    }

    /**
     * Find linked articles by studyId
     * @param studyId ID of the study
     * @return
     */
    public List<LinkedArticle> findLinkedArticleByStudyId(String studyId) {
        return linkedArticleRepository.findByStudyId(studyId);
    }

    /**
     * Save a linked article
     * @param linkedArticle linked article to be saved
     * @return
     */
    public LinkedArticle saveLinkedArticle(LinkedArticle linkedArticle) {
        return linkedArticleRepository.save(linkedArticle);
    }

    /**
     * Update a linked article
     * @param linkedArticleId ID of the linked article
     * @param updatedLinkedArticle linked article to be updated
     * @return
     */
    public Optional<LinkedArticle> updateLinkedArticle(String linkedArticleId, LinkedArticle updatedLinkedArticle) {
        Optional<LinkedArticle> oldArticle = linkedArticleRepository.findById(linkedArticleId);
        if (oldArticle.isPresent()) {
            LinkedArticle article = oldArticle.get();
            article.setArticleUrl(updatedLinkedArticle.getArticleUrl());
            article.setDescription(updatedLinkedArticle.getDescription());
            article.setStudyId(updatedLinkedArticle.getStudyId());
            LinkedArticle savedArticle = linkedArticleRepository.save(article);
            return Optional.of(savedArticle);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a linked article
     * @param linkedArticleId ID of linked article to be deleted
     * @return
     */
    public Optional<LinkedArticle> deleteLinkedArticle(String linkedArticleId) {
        Optional<LinkedArticle> existingArticle = linkedArticleRepository.findById(linkedArticleId);
        if (existingArticle.isPresent()) {
            linkedArticleRepository.delete(existingArticle.get());
            return existingArticle;
        } else {
            return Optional.empty();
        }
    }
}
