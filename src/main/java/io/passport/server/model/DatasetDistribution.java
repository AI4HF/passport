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

/**
 * One accessible form of a published dataset: where it can be reached, in what format, and
 * under which licence.
 */
@Entity
@Table(name = "dataset_distribution")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "distributionId")
public class DatasetDistribution {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String distributionId;

    @Column(name = "catalogue_dataset_id")
    private String catalogueDatasetId;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "access_url")
    private String accessUrl;

    @Column(name = "download_url")
    private String downloadUrl;

    @Column(name = "access_service_uri")
    private String accessServiceUri;

    @Column(name = "format")
    private String format;

    @Column(name = "media_type")
    private String mediaType;

    @Column(name = "availability")
    private String availability;

    @Column(name = "status")
    private String status;

    @Column(name = "licence")
    private String licence;

    @Column(name = "rights")
    private String rights;

    @Column(name = "byte_size")
    private Long byteSize;

    @Column(name = "checksum_algorithm")
    private String checksumAlgorithm;

    @Column(name = "checksum_value")
    private String checksumValue;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;

    @Column(name = "last_updated_by")
    private String lastUpdatedBy;
}
