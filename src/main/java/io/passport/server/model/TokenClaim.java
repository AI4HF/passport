package io.passport.server.model;

import lombok.Getter;

/**
 * AI4HF JWT claims
 */
@Getter
public enum TokenClaim {
    USERNAME("preferred_username");

    private String value;

    private TokenClaim(String value) {this.value = value;}
}
