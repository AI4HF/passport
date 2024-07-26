package io.passport.server.model;

import lombok.Getter;
import lombok.Setter;
import org.keycloak.representations.AccessTokenResponse;

/**
 * Response class for login request.
 */
@Getter
@Setter
public class LoginResponse {

    private AccessTokenResponse authResponse;

    private String userId;
}
