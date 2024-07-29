package io.passport.server;

import io.passport.server.controller.KeycloakController;
import io.passport.server.model.Credentials;
import io.passport.server.service.KeycloakService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.representations.AccessTokenResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.ws.rs.NotAuthorizedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link KeycloakController}.
 */
public class KeycloakControllerTest {
    @Mock
    private KeycloakService keycloakService;

    @Mock
    private Keycloak keycloak;

    @Mock
    private TokenManager tokenManager;

    @Mock
    private AccessTokenResponse accessTokenResponse;

    @InjectMocks
    private KeycloakController keycloakController;

    private Credentials credentials;

    /**
     * Sets up test data and initializes mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        credentials = new Credentials("user", "password");
    }

    /**
     * Tests the {@link KeycloakController#login(Credentials)} method.
     * Verifies that a token is returned with a status of 200 OK when credentials are valid.
     */
    @Test
    void testLoginSuccess() {
        when(keycloakService.newKeycloakBuilderWithPasswordCredentials(credentials.username, credentials.password)).thenReturn(keycloak);
        when(keycloak.tokenManager()).thenReturn(tokenManager);
        when(keycloak.tokenManager().grantToken()).thenReturn(accessTokenResponse);

        ResponseEntity<?> response = keycloakController.login(credentials);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(accessTokenResponse, response.getBody());
        verify(keycloakService, times(1)).newKeycloakBuilderWithPasswordCredentials(credentials.username, credentials.password);
    }

    /**
     * Tests the {@link KeycloakController#login(Credentials)} method.
     * Verifies that a status of 401 Unauthorized is returned when credentials are invalid.
     */
    @Test
    void testLoginUnauthorized() {
        when(keycloakService.newKeycloakBuilderWithPasswordCredentials(credentials.username, credentials.password)).thenThrow(new NotAuthorizedException("Invalid credentials"));

        ResponseEntity<?> response = keycloakController.login(credentials);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials.", response.getBody());
        verify(keycloakService, times(1)).newKeycloakBuilderWithPasswordCredentials(credentials.username, credentials.password);
    }

    /**
     * Tests the {@link KeycloakController#login(Credentials)} method.
     * Verifies that a status of 400 Bad Request is returned when credentials are missing.
     */
    @Test
    void testLoginBadRequest() {
        when(keycloakService.newKeycloakBuilderWithPasswordCredentials(credentials.username, credentials.password)).thenThrow(new IllegalStateException("Missing credentials"));

        ResponseEntity<?> response = keycloakController.login(credentials);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Missing credentials.", response.getBody());
        verify(keycloakService, times(1)).newKeycloakBuilderWithPasswordCredentials(credentials.username, credentials.password);
    }

    /**
     * Tests the {@link KeycloakController#login(Credentials)} method.
     * Verifies that a status of 500 Internal Server Error is returned when an unexpected error occurs.
     */
    @Test
    void testLoginInternalServerError() {
        when(keycloakService.newKeycloakBuilderWithPasswordCredentials(credentials.username, credentials.password)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = keycloakController.login(credentials);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(keycloakService, times(1)).newKeycloakBuilderWithPasswordCredentials(credentials.username, credentials.password);
    }
}
