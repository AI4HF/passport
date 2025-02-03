package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "auditLogId")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_log_id")
    private String auditLogId;

    @Column(name = "person_id")
    private String personId;

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