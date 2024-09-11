package io.passport.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Map;

/**
 * Entity model for storing Passport details in JSON format.
 */
@Entity
@Table(name = "passport_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PassportDetailsDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "passport_id", referencedColumnName = "passport_id")
    private Passport passport;

    // Store the rest of the details as JSON
    @Column(name = "details_json", columnDefinition = "jsonb")
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> detailsJson;
}
