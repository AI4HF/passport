package io.passport.server.model;


import lombok.Getter;

//Simple Authentication Credentials model to use in the Login method
@Getter
public class LoginRequest {

    String username;
    String password;
}
