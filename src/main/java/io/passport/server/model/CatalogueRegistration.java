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
 * The record of publishing a CatalogueDataset to one catalogue, written by the metadata
 * publisher after it posts the HealthDCAT-AP payload.
 */
@Entity
@Table(name = "catalogue_registration")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "registrationId")
public class CatalogueRegistration {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String registrationId;

    @Column(name = "catalogue_dataset_id")
    private String catalogueDatasetId;

    /**
     * Which catalogue the record was published to, e.g. the organization's FDP.
     */
    @Column(name = "catalogue_type")
    private String catalogueType;

    @Column(name = "catalogue_uri")
    private String catalogueUri;

    @Column(name = "listing_date")
    private Instant listingDate;

    /**
     * created or updated: a dataset refresh updates the existing entry in place.
     */
    @Column(name = "change_type")
    private String changeType;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;

    @Column(name = "last_updated_by")
    private String lastUpdatedBy;
}
