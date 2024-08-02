package io.passport.server;

import io.passport.server.controller.ModelDeploymentController;
import io.passport.server.model.ModelDeployment;
import io.passport.server.service.ModelDeploymentService;
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
 * Unit tests for {@link ModelDeploymentController}.
 */
public class ModelDeploymentControllerTest {
    @Mock
    private ModelDeploymentService modelDeploymentService;

    @InjectMocks
    private ModelDeploymentController modelDeploymentController;

    private ModelDeployment modelDeployment1;
    private ModelDeployment modelDeployment2;

    /**
     * Sets up test data and initializes mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        modelDeployment1 = new ModelDeployment(1L, 1L, 1L, "Tag1", "Identified Failures 1", "Status1", Instant.now(), "1", Instant.now(), "1");
        modelDeployment2 = new ModelDeployment(2L, 1L, 1L, "Tag2", "Identified Failures 2", "Status2", Instant.now(), "1", Instant.now(), "1");
    }

    /**
     * Tests the {@link ModelDeploymentController#getAllModelDeployments()} method.
     * Verifies that all modelDeployments are returned with a status of 200 OK.
     */
    @Test
    void testGetAllModelDeployments() {
        when(modelDeploymentService.getAllModelDeployments()).thenReturn(Arrays.asList(modelDeployment1, modelDeployment2));

        ResponseEntity<List<ModelDeployment>> response = modelDeploymentController.getAllModelDeployments();

        HttpHeaders headers = response.getHeaders();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        assertEquals("2", headers.getFirst("X-Total-Count"));
        verify(modelDeploymentService, times(1)).getAllModelDeployments();
    }

    /**
     * Tests the {@link ModelDeploymentController#getModelDeploymentByEnvironmentId (Long)} method.
     * Verifies that a modelDeployment is returned with a status of 200 OK when found.
     */
    @Test
    void testGetModelDeploymentByEnvironmentIdFound() {
        when(modelDeploymentService.findModelDeploymentByEnvironmentId(1L)).thenReturn(Optional.of(modelDeployment1));

        ResponseEntity<?> response = modelDeploymentController.getModelDeploymentByEnvironmentId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(modelDeployment1, response.getBody());
        verify(modelDeploymentService, times(1)).findModelDeploymentByEnvironmentId(1L);
    }

    /**
     * Tests the {@link ModelDeploymentController#getModelDeploymentByEnvironmentId (Long)} method.
     * Verifies that a status of 404 Not Found is returned when the modelDeployment is not found.
     */
    @Test
    void testGetModelDeploymentByEnvironmentIdNotFound() {
        when(modelDeploymentService.findModelDeploymentByEnvironmentId(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = modelDeploymentController.getModelDeploymentByEnvironmentId(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(modelDeploymentService, times(1)).findModelDeploymentByEnvironmentId(1L);
    }

    /**
     * Tests the {@link ModelDeploymentController#getModelDeployment(Long)} method.
     * Verifies that a modelDeployment is returned with a status of 200 OK when found.
     */
    @Test
    void testGetModelDeploymentFound() {
        when(modelDeploymentService.findModelDeploymentByDeploymentId(1L)).thenReturn(Optional.of(modelDeployment1));

        ResponseEntity<?> response = modelDeploymentController.getModelDeployment(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(modelDeployment1, response.getBody());
        verify(modelDeploymentService, times(1)).findModelDeploymentByDeploymentId(1L);
    }

    /**
     * Tests the {@link ModelDeploymentController#getModelDeploymentByEnvironmentId (Long)} method.
     * Verifies that a status of 404 Not Found is returned when the modelDeployment is not found.
     */
    @Test
    void testGetModelDeploymentNotFound() {
        when(modelDeploymentService.findModelDeploymentByDeploymentId(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = modelDeploymentController.getModelDeployment(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(modelDeploymentService, times(1)).findModelDeploymentByDeploymentId(1L);
    }

    /**
     * Tests the {@link ModelDeploymentController#createModelDeployment(ModelDeployment)} method.
     * Verifies that a modelDeployment is created successfully with a status of 201 Created.
     */
    @Test
    void testCreateModelDeploymentSuccess() {
        when(modelDeploymentService.saveModelDeployment(modelDeployment1)).thenReturn(modelDeployment1);

        ResponseEntity<?> response = modelDeploymentController.createModelDeployment(modelDeployment1);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(modelDeployment1, response.getBody());
        verify(modelDeploymentService, times(1)).saveModelDeployment(modelDeployment1);
    }

    /**
     * Tests the {@link ModelDeploymentController#createModelDeployment(ModelDeployment)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testCreateModelDeploymentFailure() {
        when(modelDeploymentService.saveModelDeployment(modelDeployment1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = modelDeploymentController.createModelDeployment(modelDeployment1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(modelDeploymentService, times(1)).saveModelDeployment(modelDeployment1);
    }

    /**
     * Tests the {@link ModelDeploymentController#updateModelDeployment(Long, ModelDeployment)} method.
     * Verifies that a modelDeployment is updated successfully with a status of 200 OK when found.
     */
    @Test
    void testUpdateModelDeploymentFound() {
        when(modelDeploymentService.updateModelDeployment(1L, modelDeployment1)).thenReturn(Optional.of(modelDeployment1));

        ResponseEntity<?> response = modelDeploymentController.updateModelDeployment(1L, modelDeployment1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Optional.of(modelDeployment1), response.getBody());
        verify(modelDeploymentService, times(1)).updateModelDeployment(1L, modelDeployment1);
    }

    /**
     * Tests the {@link ModelDeploymentController#updateModelDeployment(Long, ModelDeployment)} method.
     * Verifies that a status of 404 Not Found is returned when the modelDeployment to update is not found.
     */
    @Test
    void testUpdateModelDeploymentNotFound() {
        when(modelDeploymentService.updateModelDeployment(1L, modelDeployment1)).thenReturn(Optional.empty());

        ResponseEntity<?> response = modelDeploymentController.updateModelDeployment(1L, modelDeployment1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(modelDeploymentService, times(1)).updateModelDeployment(1L, modelDeployment1);
    }

    /**
     * Tests the {@link ModelDeploymentController#updateModelDeployment(Long, ModelDeployment)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testUpdateModelDeploymentFailure() {
        when(modelDeploymentService.updateModelDeployment(1L, modelDeployment1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = modelDeploymentController.updateModelDeployment(1L, modelDeployment1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(modelDeploymentService, times(1)).updateModelDeployment(1L, modelDeployment1);
    }

    /**
     * Tests the {@link ModelDeploymentController#deleteModelDeployment(Long)} method.
     * Verifies that a modelDeployment is deleted successfully with a status of 204 No Content when found.
     */
    @Test
    void testDeleteModelDeploymentFound() {
        when(modelDeploymentService.deleteModelDeployment(1L)).thenReturn(true);

        ResponseEntity<?> response = modelDeploymentController.deleteModelDeployment(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(modelDeploymentService, times(1)).deleteModelDeployment(1L);
    }

    /**
     * Tests the {@link ModelDeploymentController#deleteModelDeployment(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the modelDeployment to delete is not found.
     */
    @Test
    void testDeleteModelDeploymentNotFound() {
        when(modelDeploymentService.deleteModelDeployment(1L)).thenReturn(false);

        ResponseEntity<?> response = modelDeploymentController.deleteModelDeployment(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(modelDeploymentService, times(1)).deleteModelDeployment(1L);
    }

    /**
     * Tests the {@link ModelDeploymentController#deleteModelDeployment(Long)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testDeleteModelDeploymentFailure() {
        when(modelDeploymentService.deleteModelDeployment(1L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = modelDeploymentController.deleteModelDeployment(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(modelDeploymentService, times(1)).deleteModelDeployment(1L);
    }
}
