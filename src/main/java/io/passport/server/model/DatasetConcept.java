package io.passport.server.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

/**
 * A controlled-vocabulary value on a Dataset: one of the multi-valued HealthDCAT-AP
 * properties the Data Steward completes, such as health category or theme.
 */
@Entity
@Table(name = "dataset_concept")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "conceptId")
public class DatasetConcept {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String conceptId;

    @Column(name = "dataset_id")
    private String datasetId;

    /**
     * The HealthDCAT-AP property this value fills, e.g. the health category.
     */
    @Column(name = "property_uri")
    private String propertyUri;

    @Column(name = "concept_uri")
    private String conceptUri;

    @Column(name = "pref_label")
    private String prefLabel;

    @Column(name = "concept_scheme")
    private String conceptScheme;
}
