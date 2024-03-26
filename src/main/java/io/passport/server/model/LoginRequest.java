package io.passport.server.model;

import lombok.Getter;

/**
 * Simple Authentication Credentials model to use in the Login method
 * */
@Getter
public class LoginRequest {
    /**
     * Usename credential of a logging in user. Acts as a both non-unique identifier in case of requests
     */
    String username;
    /**
     * Password credentials of a logging in user. Is checked to see if it fits with the username provided.
     */
    String password;
}