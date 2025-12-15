package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

/**
 * LinkedArticle model used for the Linked Article Management tasks.
 */
@Entity
@Table(name = "linked_article")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "linkedArticleId")
public class LinkedArticle {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "linked_article_id")
    private String linkedArticleId;

    @Column(name = "study_id")
    private String studyId;

    @Column(name = "article_url")
    private String articleUrl;

    @Column(name = "description")
    private String description;
}
