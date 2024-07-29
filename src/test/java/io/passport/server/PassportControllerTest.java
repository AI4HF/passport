package io.passport.server;

import io.passport.server.controller.PassportController;
import io.passport.server.model.Passport;
import io.passport.server.service.PassportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PassportController}.
 */
public class PassportControllerTest {
    @Mock
    private PassportService passportService;

    @InjectMocks
    private PassportController passportController;

    private Passport passport1;
    private Passport passport2;

    /**
     * Sets up test data and initializes mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        passport1 = new Passport(1L, 1L, Instant.now(), 1L, Instant.now(), 1L);
        passport2 = new Passport(2L, 1L, Instant.now(), 1L, Instant.now(), 1L);
    }

    /**
     * Tests the {@link PassportController#getAllPassports()} method.
     * Verifies that all passports are returned with a status of 200 OK.
     */
    @Test
    void testGetAllPassports() {
        when(passportService.getAllPassports()).thenReturn(Arrays.asList(passport1, passport2));

        ResponseEntity<List<Passport>> response = passportController.getAllPassports();

        HttpHeaders headers = response.getHeaders();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        assertEquals("2", headers.getFirst("X-Total-Count"));
        verify(passportService, times(1)).getAllPassports();
    }

    /**
     * Tests the {@link PassportController#getPassport(Long)} method.
     * Verifies that a passport is returned with a status of 200 OK when found.
     */
    @Test
    void testGetPassportFound() {
        when(passportService.findPassportByPassportId(1L)).thenReturn(Optional.of(passport1));

        ResponseEntity<?> response = passportController.getPassport(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(passport1, response.getBody());
        verify(passportService, times(1)).findPassportByPassportId(1L);
    }

    /**
     * Tests the {@link PassportController#getPassport(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the passport is not found.
     */
    @Test
    void testGetPassportNotFound() {
        when(passportService.findPassportByPassportId(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = passportController.getPassport(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(passportService, times(1)).findPassportByPassportId(1L);
    }

    /**
     * Tests the {@link PassportController#createPassport(Passport)} method.
     * Verifies that a passport is created successfully with a status of 201 Created.
     */
    @Test
    void testCreatePassportSuccess() {
        when(passportService.savePassport(passport1)).thenReturn(passport1);

        ResponseEntity<?> response = passportController.createPassport(passport1);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(passport1, response.getBody());
        verify(passportService, times(1)).savePassport(passport1);
    }

    /**
     * Tests the {@link PassportController#createPassport(Passport)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testCreatePassportFailure() {
        when(passportService.savePassport(passport1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = passportController.createPassport(passport1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(passportService, times(1)).savePassport(passport1);
    }

    /**
     * Tests the {@link PassportController#deletePassport(Long)} method.
     * Verifies that a passport is deleted successfully with a status of 204 No Content when found.
     */
    @Test
    void testDeletePassportFound() {
        when(passportService.deletePassport(1L)).thenReturn(true);

        ResponseEntity<?> response = passportController.deletePassport(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(passportService, times(1)).deletePassport(1L);
    }

    /**
     * Tests the {@link PassportController#deletePassport(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the passport to delete is not found.
     */
    @Test
    void testDeletePassportNotFound() {
        when(passportService.deletePassport(1L)).thenReturn(false);

        ResponseEntity<?> response = passportController.deletePassport(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(passportService, times(1)).deletePassport(1L);
    }

    /**
     * Tests the {@link PassportController#deletePassport(Long)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testDeletePassportFailure() {
        when(passportService.deletePassport(1L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = passportController.deletePassport(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(passportService, times(1)).deletePassport(1L);
    }
}
