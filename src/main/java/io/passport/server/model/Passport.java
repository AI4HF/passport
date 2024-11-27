package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

/**
 * Passport model used for the Passport Management tasks.
 */
@Entity
@Table(name = "passport")
@Getter
@Setter
public class Passport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "passport_id")
    private Long passportId;

    @Column(name = "study_id")
    private Long studyId;

    @Column(name = "deployment_id")
    private Long deploymentId;

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
}
