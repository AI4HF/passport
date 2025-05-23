package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;

/**
 * Model class for Model Table.
 */
@Entity
@Table(name = "model")
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "modelId")
public class Model {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String modelId;

    @Column(name = "learning_process_id")
    private String learningProcessId;

    @Column(name = "study_id")
    private String studyId;

    @Column(name = "experiment_id")
    private String experimentId;

    @Column(name = "name")
    private String name;

    @Column(name = "version")
    private String version;

    @Column(name = "tag")
    private String tag;

    @Column(name = "model_type")
    private String modelType;

    @Column(name = "product_identifier")
    private String productIdentifier;

    @Column(name = "owner")
    private String owner;

    @Column(name = "trl_level")
    private String trlLevel;

    @Column(name = "license")
    private String license;

    @Column(name = "primary_use")
    private String primaryUse;

    @Column(name = "secondary_use")
    private String secondaryUse;

    @Column(name = "intended_users")
    private String intendedUsers;

    @Column(name = "counter_indications")
    private String counterIndications;

    @Column(name = "ethical_considerations")
    private String ethicalConsiderations;

    @Column(name = "limitations")
    private String limitations;

    @Column(name = "fairness_constraints")
    private String fairnessConstraints;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;

    @Column(name = "last_updated_by")
    private String lastUpdatedBy;
}
