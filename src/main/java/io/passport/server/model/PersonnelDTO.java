package io.passport.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Personnel DTO model for creating personnel and a keycloak user.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PersonnelDTO {

    private Personnel personnel;

    private Credentials credentials;

    private Boolean isStudyOwner;
}
