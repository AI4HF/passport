package io.passport.server.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * ModelDto model used for storing associated deploymentId/passportId of Model.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModelDto {

    private Long id;

    private Long modelId;

    private Long learningProcessId;

    private Long studyId;

    private String name;

    private String version;

    private String tag;

    private String modelType;

    private String productIdentifier;

    private Long owner;

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

    private Long createdBy;

    private Instant lastUpdatedAt;

    private Long lastUpdatedBy;
}
