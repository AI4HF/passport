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
 * Audit Log model used for Audit Logging.
 */
@Entity
@Table(name = "audit_log")
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "auditLogId")
public class AuditLog {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String auditLogId;

    @Column(name = "person_id")
    private String personId;

    @Column(name = "person_name")
    private String personName;

    @Column(name = "study_id")
    private Long studyId;

    @Column(name = "occurred_at")
    private Instant occurredAt;

    @Column(name = "action_type")
    private String actionType;

    @Column(name = "affected_relation")
    private String affectedRelation;

    @Column(name = "affected_record_id")
    private String affectedRecordId;

    @Column(name = "affected_record")
    private String affectedRecord;

    @Column(name = "description")
    private String description;
}