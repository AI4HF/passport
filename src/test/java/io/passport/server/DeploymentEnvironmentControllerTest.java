package io.passport.server;

import io.passport.server.controller.DeploymentEnvironmentController;
import io.passport.server.model.DeploymentEnvironment;
import io.passport.server.service.DeploymentEnvironmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DeploymentEnvironmentController}.
 */
public class DeploymentEnvironmentControllerTest {
    @Mock
    private DeploymentEnvironmentService deploymentEnvironmentService;

    @InjectMocks
    private DeploymentEnvironmentController deploymentEnvironmentController;

    private DeploymentEnvironment deploymentEnvironment1;
    private DeploymentEnvironment deploymentEnvironment2;

    /**
     * Sets up test data and initializes mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        deploymentEnvironment1 = new DeploymentEnvironment(1L, "Title 1", "Description 1", "Hardware Properties 1", "Software Properties 1", "Connectivity Details 1");
        deploymentEnvironment1 = new DeploymentEnvironment(2L, "Title 2", "Description 2", "Hardware Properties 2", "Software Properties 2", "Connectivity Details 2");
    }

    /**
     * Tests the {@link DeploymentEnvironmentController#getDeploymentEnvironmentByEnvironmentId(Long)} method.
     * Verifies that a deploymentEnvironment is returned with a status of 200 OK when found.
     */
    @Test
    void testGetDeploymentEnvironmentByEnvironmentIdFound() {
        when(deploymentEnvironmentService.findDeploymentEnvironmentById(1L)).thenReturn(Optional.of(deploymentEnvironment1));

        ResponseEntity<?> response = deploymentEnvironmentController.getDeploymentEnvironmentByEnvironmentId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Optional.of(deploymentEnvironment1), response.getBody());
        verify(deploymentEnvironmentService, times(1)).findDeploymentEnvironmentById(1L);
    }

    /**
     * Tests the {@link DeploymentEnvironmentController#getDeploymentEnvironmentByEnvironmentId(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the deploymentEnvironment is not found.
     */
    @Test
    void testGetDeploymentEnvironmentByEnvironmentIdNotFound() {
        when(deploymentEnvironmentService.findDeploymentEnvironmentById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = deploymentEnvironmentController.getDeploymentEnvironmentByEnvironmentId(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(deploymentEnvironmentService, times(1)).findDeploymentEnvironmentById(1L);
    }

    /**
     * Tests the {@link DeploymentEnvironmentController#createDeploymentEnvironment(DeploymentEnvironment)} method.
     * Verifies that a deploymentEnvironment is created successfully with a status of 201 Created.
     */
    @Test
    void testCreateDeploymentEnvironmentSuccess() {
        when(deploymentEnvironmentService.saveDevelopmentEnvironment(deploymentEnvironment1)).thenReturn(deploymentEnvironment1);

        ResponseEntity<?> response = deploymentEnvironmentController.createDeploymentEnvironment(deploymentEnvironment1);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(deploymentEnvironment1, response.getBody());
        verify(deploymentEnvironmentService, times(1)).saveDevelopmentEnvironment(deploymentEnvironment1);
    }

    /**
     * Tests the {@link DeploymentEnvironmentController#createDeploymentEnvironment(DeploymentEnvironment)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testCreateDeploymentEnvironmentFailure() {
        when(deploymentEnvironmentService.saveDevelopmentEnvironment(deploymentEnvironment1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = deploymentEnvironmentController.createDeploymentEnvironment(deploymentEnvironment1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(deploymentEnvironmentService, times(1)).saveDevelopmentEnvironment(deploymentEnvironment1);
    }

    /**
     * Tests the {@link DeploymentEnvironmentController#updateDeploymentEnvironment(Long, DeploymentEnvironment)} method.
     * Verifies that a deploymentEnvironment is updated successfully with a status of 200 OK when found.
     */
    @Test
    void testUpdateDeploymentEnvironmentFound() {
        when(deploymentEnvironmentService.updateDeploymentEnvironment(1L, deploymentEnvironment1)).thenReturn(Optional.of(deploymentEnvironment1));

        ResponseEntity<?> response = deploymentEnvironmentController.updateDeploymentEnvironment(1L, deploymentEnvironment1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(deploymentEnvironment1, response.getBody());
        verify(deploymentEnvironmentService, times(1)).updateDeploymentEnvironment(1L, deploymentEnvironment1);
    }

    /**
     * Tests the {@link DeploymentEnvironmentController#updateDeploymentEnvironment(Long, DeploymentEnvironment)} method.
     * Verifies that a status of 404 Not Found is returned when the deploymentEnvironment to update is not found.
     */
    @Test
    void testUpdateDeploymentEnvironmentNotFound() {
        when(deploymentEnvironmentService.updateDeploymentEnvironment(1L, deploymentEnvironment1)).thenReturn(Optional.empty());

        ResponseEntity<?> response = deploymentEnvironmentController.updateDeploymentEnvironment(1L, deploymentEnvironment1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(deploymentEnvironmentService, times(1)).updateDeploymentEnvironment(1L, deploymentEnvironment1);
    }

    /**
     * Tests the {@link DeploymentEnvironmentController#updateDeploymentEnvironment(Long, DeploymentEnvironment)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testUpdateDeploymentEnvironmentFailure() {
        when(deploymentEnvironmentService.updateDeploymentEnvironment(1L, deploymentEnvironment1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = deploymentEnvironmentController.updateDeploymentEnvironment(1L, deploymentEnvironment1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(deploymentEnvironmentService, times(1)).updateDeploymentEnvironment(1L, deploymentEnvironment1);
    }

    /**
     * Tests the {@link DeploymentEnvironmentController#(Long)} method.
     * Verifies that a deploymentEnvironment is deleted successfully with a status of 204 No Content when found.
     */
    @Test
    void testDeleteDeploymentEnvironmentFound() {
        when(deploymentEnvironmentService.deleteDeploymentEnvironment(1L)).thenReturn(true);

        ResponseEntity<?> response = deploymentEnvironmentController.deleteDeploymentEnvironment(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(deploymentEnvironmentService, times(1)).deleteDeploymentEnvironment(1L);
    }

    /**
     * Tests the {@link DeploymentEnvironmentController#deleteDeploymentEnvironment(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the deploymentEnvironment to delete is not found.
     */
    @Test
    void testDeleteDeploymentEnvironmentNotFound() {
        when(deploymentEnvironmentService.deleteDeploymentEnvironment(1L)).thenReturn(false);

        ResponseEntity<?> response = deploymentEnvironmentController.deleteDeploymentEnvironment(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(deploymentEnvironmentService, times(1)).deleteDeploymentEnvironment(1L);
    }

    /**
     * Tests the {@link DeploymentEnvironmentController#deleteDeploymentEnvironment(Long)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testDeleteDeploymentEnvironmentFailure() {
        when(deploymentEnvironmentService.deleteDeploymentEnvironment(1L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = deploymentEnvironmentController.deleteDeploymentEnvironment(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(deploymentEnvironmentService, times(1)).deleteDeploymentEnvironment(1L);
    }
}
