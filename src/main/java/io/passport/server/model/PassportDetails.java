package io.passport.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PassportDetails class that holds which fields will be populated in the Passport
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PassportDetails {

    private boolean modelDetails;

    private boolean modelDeploymentDetails;

    private boolean environmentDetails;

    private boolean datasets;

    private boolean featureSets;

    private boolean learningProcessDetails;

    private boolean parameterDetails;

    private boolean populationDetails;

    private boolean experimentDetails;

    private boolean surveyDetails;

    private boolean studyDetails;
}
