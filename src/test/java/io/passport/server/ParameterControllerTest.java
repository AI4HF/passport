package io.passport.server;

import io.passport.server.controller.ParameterController;
import io.passport.server.model.Parameter;
import io.passport.server.service.ParameterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ParameterController}.
 */
public class ParameterControllerTest {
    @Mock
    private ParameterService parameterService;

    @InjectMocks
    private ParameterController parameterController;

    private Parameter parameter1;
    private Parameter parameter2;

    /**
     * Sets up test data and initializes mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        parameter1 = new Parameter(1L, "Parameter Name 1", "String", "Description 1");
        parameter2 = new Parameter(2L, "Parameter Name 2", "Integer", "Description 2");
    }

    /**
     * Tests the {@link ParameterController#getAllParameters()} method.
     * Verifies that all parameters are returned with a status of 200 OK.
     */
    @Test
    void testGetAllParameters() {
        when(parameterService.getAllParameters()).thenReturn(Arrays.asList(parameter1, parameter2));

        ResponseEntity<List<Parameter>> response = parameterController.getAllParameters();

        HttpHeaders headers = response.getHeaders();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        assertEquals("2", headers.getFirst("X-Total-Count"));
        verify(parameterService, times(1)).getAllParameters();
    }

    /**
     * Tests the {@link ParameterController#getParameterById (Long)} method.
     * Verifies that a parameter is returned with a status of 200 OK when found.
     */
    @Test
    void testGetParameterByIdFound() {
        when(parameterService.findParameterById(1L)).thenReturn(Optional.of(parameter1));

        ResponseEntity<?> response = parameterController.getParameterById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Optional.of(parameter1), response.getBody());
        verify(parameterService, times(1)).findParameterById(1L);
    }

    /**
     * Tests the {@link ParameterController#getParameterById(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the parameter is not found.
     */
    @Test
    void testGetParameterByIdNotFound() {
        when(parameterService.findParameterById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = parameterController.getParameterById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(parameterService, times(1)).findParameterById(1L);
    }

    /**
     * Tests the {@link ParameterController#createParameter(Parameter)} method.
     * Verifies that a parameter is created successfully with a status of 201 Created.
     */
    @Test
    void testCreateParameterSuccess() {
        when(parameterService.saveParameter(parameter1)).thenReturn(parameter1);

        ResponseEntity<?> response = parameterController.createParameter(parameter1);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(parameter1, response.getBody());
        verify(parameterService, times(1)).saveParameter(parameter1);
    }

    /**
     * Tests the {@link ParameterController#createParameter(Parameter)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testCreateParameterFailure() {
        when(parameterService.saveParameter(parameter1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = parameterController.createParameter(parameter1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(parameterService, times(1)).saveParameter(parameter1);
    }

    /**
     * Tests the {@link ParameterController#updateParameter(Long, Parameter)} method.
     * Verifies that a parameter is updated successfully with a status of 200 OK when found.
     */
    @Test
    void testUpdateParameterFound() {
        when(parameterService.updateParameter(1L, parameter1)).thenReturn(Optional.of(parameter1));

        ResponseEntity<?> response = parameterController.updateParameter(1L, parameter1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(parameter1, response.getBody());
        verify(parameterService, times(1)).updateParameter(1L, parameter1);
    }

    /**
     * Tests the {@link ParameterController#updateParameter(Long, Parameter)} method.
     * Verifies that a status of 404 Not Found is returned when the parameter to update is not found.
     */
    @Test
    void testUpdateParameterNotFound() {
        when(parameterService.updateParameter(1L, parameter1)).thenReturn(Optional.empty());

        ResponseEntity<?> response = parameterController.updateParameter(1L, parameter1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(parameterService, times(1)).updateParameter(1L, parameter1);
    }

    /**
     * Tests the {@link ParameterController#updateParameter(Long, Parameter)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testUpdateParameterFailure() {
        when(parameterService.updateParameter(1L, parameter1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = parameterController.updateParameter(1L, parameter1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(parameterService, times(1)).updateParameter(1L, parameter1);
    }

    /**
     * Tests the {@link ParameterController#deleteParameter(Long)} method.
     * Verifies that a parameter is deleted successfully with a status of 204 No Content when found.
     */
    @Test
    void testDeleteParameterFound() {
        when(parameterService.deleteParameter(1L)).thenReturn(true);

        ResponseEntity<?> response = parameterController.deleteParameter(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(parameterService, times(1)).deleteParameter(1L);
    }

    /**
     * Tests the {@link ParameterController#deleteParameter(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the parameter to delete is not found.
     */
    @Test
    void testDeleteParameterNotFound() {
        when(parameterService.deleteParameter(1L)).thenReturn(false);

        ResponseEntity<?> response = parameterController.deleteParameter(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(parameterService, times(1)).deleteParameter(1L);
    }

    /**
     * Tests the {@link ParameterController#deleteParameter(Long)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testDeleteParameterFailure() {
        when(parameterService.deleteParameter(1L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = parameterController.deleteParameter(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(parameterService, times(1)).deleteParameter(1L);
    }
}
