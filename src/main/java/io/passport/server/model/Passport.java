package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.util.Map;

/**
 * Passport model used for the Passport Management tasks.
 */
@Entity
@Table(name = "passport")
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "passportId")
public class Passport {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String passportId;

    @Column(name = "study_id")
    private String studyId;

    @Column(name = "model_id")
    private String modelId;

    /**
     * Passports are never rewritten: regenerating one produces a new version linked to the previous,
     * mirroring the Model lineage the passport documents.
     */
    @Column(name = "version")
    private Integer version;

    @Column(name = "previous_passport_id")
    private String previousPassportId;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "approved_by")
    private String approvedBy;

    // TODO: Bad practice. Reflect on the method of storing passport details data. Think about storing it using the file system and providing a reference.
    @Column(name = "details_json", columnDefinition = "jsonb")
    @Convert(converter = JsonConverter.class)
    @ColumnTransformer(write = "?::jsonb")
    private Map<String, Object> detailsJson;

    /**
     * The signed PDF exactly as it was produced at generation time. It is kept out of every JSON response -
     * it is served by its own download endpoint - so listing passports stays cheap.
     */
    @JsonIgnore
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "signed_pdf", columnDefinition = "bytea")
    private byte[] signedPdf;

    /**
     * SHA-256 of signedPdf, so a downloaded document can be checked against the record without the record
     * having to hand out the bytes.
     */
    @Column(name = "signed_pdf_hash")
    private String signedPdfHash;
}
