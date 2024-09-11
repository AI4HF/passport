package io.passport.server.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
/**
 * DTO that encapsulates a FeatureSet and its associated Features.
 */
public class FeatureSetWithFeaturesDTO {

    /**
     * The FeatureSet entity.
     */
    private FeatureSet featureSet;

    /**
     * The list of Features associated with the FeatureSet.
     */
    private List<Feature> features;

    // Getters and Setters

    public FeatureSet getFeatureSet() {
        return featureSet;
    }

    public void setFeatureSet(FeatureSet featureSet) {
        this.featureSet = featureSet;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }
}
