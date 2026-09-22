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
            case "Model":
                affectedArticles = linkedArticleRepository.findByModelId(sourceResourceId);
                break;
            default:
                return new ValidationResult(true, "");
        }

        if (affectedArticles.isEmpty()) {
            return new ValidationResult(true, "");
        }

        boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                studyId,
                principal,
                List.of(Role.DATA_SCIENTIST)
        );

        if (!hasPermission) {
            return new ValidationResult(false, "LinkedArticle");
        }

        return new ValidationResult(true, "LinkedArticle");
    }
    public List<LinkedArticle> findAllLinkedArticles() {
        return linkedArticleRepository.findAll();
    }

    /**
     * Find a linked article by articleId
     * @param articleId ID of the linked article
     * @return
     */
    public Optional<LinkedArticle> findLinkedArticleById(String articleId) {
        return linkedArticleRepository.findById(articleId);
    }

    /**
     * Find linked articles by modelId
     * @param modelId ID of the model
     * @return
     */
    public List<LinkedArticle> findLinkedArticleByModelId(String modelId) {
        return linkedArticleRepository.findByModelId(modelId);
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
     * @param articleId ID of the linked article
     * @param updatedLinkedArticle linked article to be updated
     * @return
     */
    public Optional<LinkedArticle> updateLinkedArticle(String articleId, LinkedArticle updatedLinkedArticle) {
        Optional<LinkedArticle> oldArticle = linkedArticleRepository.findById(articleId);
        if (oldArticle.isPresent()) {
            LinkedArticle article = oldArticle.get();
            article.setModelId(updatedLinkedArticle.getModelId());
            article.setDoi(updatedLinkedArticle.getDoi());
            article.setTitle(updatedLinkedArticle.getTitle());
            article.setAuthors(updatedLinkedArticle.getAuthors());
            article.setPublicationVenue(updatedLinkedArticle.getPublicationVenue());
            article.setPublicationYear(updatedLinkedArticle.getPublicationYear());
            article.setUrl(updatedLinkedArticle.getUrl());
            article.setDescription(updatedLinkedArticle.getDescription());
            LinkedArticle savedArticle = linkedArticleRepository.save(article);
            return Optional.of(savedArticle);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a linked article
     * @param articleId ID of linked article to be deleted
     * @return
     */
    public Optional<LinkedArticle> deleteLinkedArticle(String articleId) {
        Optional<LinkedArticle> existingArticle = linkedArticleRepository.findById(articleId);
        if (existingArticle.isPresent()) {
            linkedArticleRepository.delete(existingArticle.get());
            return existingArticle;
        } else {
            return Optional.empty();
        }
    }
}
