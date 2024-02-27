package com.backend.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.backend.config.KeycloakProvider;
import com.backend.model.LoginRequest;


import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final KeycloakProvider kcProvider;

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(UserController.class);


    public UserController(KeycloakProvider kcProvider) {
        this.kcProvider = kcProvider;

    }


    @PostMapping("/login")
    public ResponseEntity<List<String>> login(@NotNull @RequestBody LoginRequest loginRequest) {
        Keycloak keycloak = kcProvider.newKeycloakBuilderWithPasswordCredentials(loginRequest.getUsername(), loginRequest.getPassword()).build();

        AccessTokenResponse accessTokenResponse = null;
        try {
            accessTokenResponse = keycloak.tokenManager().getAccessToken();
            String accessToken = accessTokenResponse.getToken();
            DecodedJWT decodedJWT = JWT.decode(accessToken);

            Map<String, Object> resourceAccess = decodedJWT.getClaim("resource_access").asMap();
            Map<String, Object> clientRoles = (Map<String, Object>) resourceAccess.get(kcProvider.clientID);

            List<String> roles = new ArrayList<>();
            if (clientRoles != null && clientRoles.containsKey("roles")) {
                roles.addAll((List<String>) clientRoles.get("roles"));
            }

            return ResponseEntity.status(HttpStatus.OK).body(roles);
        } catch (BadRequestException ex) {
            LOG.warn("invalid account creds.", ex);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.emptyList());
        }
    }




}