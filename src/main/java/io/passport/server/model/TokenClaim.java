package io.passport.server.model;

import lombok.Getter;

/**
 * AI4HF JWT claims
 */
@Getter
public enum TokenClaim {
    USERNAME("preferred_username"),
    /** Authorized party: the Keycloak client the token was issued to. */
    AUTHORIZED_PARTY("azp");

    private String value;

    private TokenClaim(String value) {this.value = value;}
}
