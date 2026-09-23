package io.passport.server.model;

/**
 * Kind of actor an audit log entry is attributed to: an interactive user, or an automated
 * integration authenticating with a Keycloak service account.
 */
public enum ActorType {
    PERSONNEL,
    SOFTWARE_AGENT
}
