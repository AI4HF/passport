package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Model class for Model DTO.
 */
@Getter
@Setter
public class ModelWithOwnerNameDTO {

    private String modelId;

    private String learningProcessId;

    private String studyId;

    private String experimentId;

    private String name;

    private String version;

    private String tag;

    private String modelType;

    private String productIdentifier;

    private String owner;

    private String trlLevel;

    private String license;

    private String primaryUse;

    private String secondaryUse;

    private String intendedUsers;

    private String counterIndications;

    private String ethicalConsiderations;

    private String limitations;

    private String fairnessConstraints;

    private Instant createdAt;

    private String createdBy;

    private Instant lastUpdatedAt;

    private String lastUpdatedBy;

    public ModelWithOwnerNameDTO(Model model) {
        this.modelId = model.getModelId();
        this.learningProcessId = model.getLearningProcessId();
        this.studyId = model.getStudyId();
        this.experimentId = model.getExperimentId();
        this.name = model.getName();
        this.version = model.getVersion();
        this.tag = model.getTag();
        this.modelType = model.getModelType();
        this.productIdentifier = model.getProductIdentifier();
        this.owner = model.getOwner();
        this.trlLevel = model.getTrlLevel();
        this.license = model.getLicense();
        this.primaryUse = model.getPrimaryUse();
        this.secondaryUse = model.getSecondaryUse();
        this.intendedUsers = model.getIntendedUsers();
        this.counterIndications = model.getCounterIndications();
        this.ethicalConsiderations = model.getEthicalConsiderations();
        this.limitations = model.getLimitations();
        this.fairnessConstraints = model.getFairnessConstraints();
        this.createdAt = model.getCreatedAt();
        this.createdBy = model.getCreatedBy();
    }
}
