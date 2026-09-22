package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.time.LocalDate;

/**
 * The decision to publish a Dataset, with the metadata that exists only for publication.
 * Its presence is what makes a dataset publishable; a dataset without one stays internal.
 */
@Entity
@Table(name = "catalogue_dataset")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "catalogueDatasetId")
public class CatalogueDataset {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String catalogueDatasetId;

    @Column(name = "dataset_id")
    private String datasetId;

    @Column(name = "public_title")
    private String publicTitle;

    @Column(name = "public_description")
    private String publicDescription;

    @Column(name = "access_rights")
    private String accessRights;

    /**
     * The Health Data Access Body responsible for the dataset.
     */
    @Column(name = "hdab_name")
    private String hdabName;

    @Column(name = "hdab_uri")
    private String hdabUri;

    @Column(name = "publisher_name")
    private String publisherName;

    @Column(name = "publisher_type")
    private String publisherType;

    @Column(name = "contact_point")
    private String contactPoint;

    @Column(name = "legal_basis")
    private String legalBasis;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "personal_data")
    private Boolean personalData;

    @Column(name = "publication_approval_reference")
    private String publicationApprovalReference;

    @Column(name = "applicable_legislation")
    private String applicableLegislation;

    @Column(name = "retention_period_start")
    private LocalDate retentionPeriodStart;

    @Column(name = "retention_period_end")
    private LocalDate retentionPeriodEnd;

    @Column(name = "landing_page")
    private String landingPage;

    @Column(name = "documentation")
    private String documentation;

    @Column(name = "quality_annotation")
    private String qualityAnnotation;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;

    @Column(name = "last_updated_by")
    private String lastUpdatedBy;
}
