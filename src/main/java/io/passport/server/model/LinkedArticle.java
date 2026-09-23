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
 * A scientific publication linked to the ML model by the researchers - the paper describing the model or
 * a study validating it. The citation is held in structured fields so the passport can render it rather
 * than only link it.
 */
@Entity
@Table(name = "linked_article")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "articleId")
public class LinkedArticle {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "article_id")
    private String articleId;

    @Column(name = "model_id")
    private String modelId;

    @Column(name = "doi")
    private String doi;

    @Column(name = "title")
    private String title;

    @Column(name = "authors")
    private String authors;

    @Column(name = "publication_venue")
    private String publicationVenue;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(name = "url")
    private String url;

    @Column(name = "description")
    private String description;
}
