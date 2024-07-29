package io.passport.server;

import io.passport.server.controller.DatasetTransformationStepController;
import io.passport.server.model.DatasetTransformationStep;
import io.passport.server.service.DatasetTransformationStepService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DatasetTransformationStepController}.
 */
public class DatasetTransformationStepControllerTest {
    @Mock
    private DatasetTransformationStepService datasetTransformationStepService;

    @InjectMocks
    private DatasetTransformationStepController datasetTransformationStepController;

    private DatasetTransformationStep datasetTransformationStep1;
    private DatasetTransformationStep datasetTransformationStep2;

    /**
     * Sets up test data and initializes mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        datasetTransformationStep1 = new DatasetTransformationStep(1L, 1L,"input_features_example 1","output_features_example 2", "method_example 1","explanation_example 1", LocalDateTime.now(),1L, LocalDateTime.now(),1L);
        datasetTransformationStep2 = new DatasetTransformationStep(2L, 1L,"input_features_example 1","output_features_example 2", "method_example 2","explanation_example 2", LocalDateTime.now(),1L, LocalDateTime.now(),1L);
    }

    /**
     * Tests the {@link DatasetTransformationStepController#getAllDatasetTransformationSteps()} method.
     * Verifies that all datasetTransformationSteps are returned with a status of 200 OK.
     */
    @Test
    void testGetAllDatasetTransformationSteps() {
        when(datasetTransformationStepService.getAllDatasetTransformationSteps()).thenReturn(Arrays.asList(datasetTransformationStep1, datasetTransformationStep2));

        ResponseEntity<List<DatasetTransformationStep>> response = datasetTransformationStepController.getAllDatasetTransformationSteps();

        HttpHeaders headers = response.getHeaders();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        assertEquals("2", headers.getFirst("X-Total-Count"));
        verify(datasetTransformationStepService, times(1)).getAllDatasetTransformationSteps();
    }

    /**
     * Tests the {@link DatasetTransformationStepController#getDatasetTransformationStepsByTransformationId(Long)} method.
     * Verifies that all datasetTransformationSteps with given transformationId are returned with a status of 200 OK.
     */
    @Test
    void testGetDatasetTransformationStepsByTransformationId() {
        when(datasetTransformationStepService.findByDataTransformationId(1L)).thenReturn(Arrays.asList(datasetTransformationStep1, datasetTransformationStep2));

        ResponseEntity<List<DatasetTransformationStep>> response = datasetTransformationStepController.getDatasetTransformationStepsByTransformationId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        verify(datasetTransformationStepService, times(1)).findByDataTransformationId(1L);
    }

    /**
     * Tests the {@link DatasetTransformationStepController#getDatasetTransformationStep(Long)} method.
     * Verifies that a datasetTransformationStep is returned with a status of 200 OK when found.
     */
    @Test
    void testGetDatasetTransformationStepFound() {
        when(datasetTransformationStepService.findDatasetTransformationStepByStepId(1L)).thenReturn(Optional.of(datasetTransformationStep1));

        ResponseEntity<?> response = datasetTransformationStepController.getDatasetTransformationStep(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(datasetTransformationStep1, response.getBody());
        verify(datasetTransformationStepService, times(1)).findDatasetTransformationStepByStepId(1L);
    }

    /**
     * Tests the {@link DatasetTransformationStepController#getDatasetTransformationStep(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the datasetTransformationStep is not found.
     */
    @Test
    void testGetDatasetTransformationStepNotFound() {
        when(datasetTransformationStepService.findDatasetTransformationStepByStepId(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = datasetTransformationStepController.getDatasetTransformationStep(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(datasetTransformationStepService, times(1)).findDatasetTransformationStepByStepId(1L);
    }

    /**
     * Tests the {@link DatasetTransformationStepController#createDatasetTransformationStep(DatasetTransformationStep)} method.
     * Verifies that a datasetTransformationStep is created successfully with a status of 201 Created.
     */
    @Test
    void testCreateDatasetTransformationStepSuccess() {
        when(datasetTransformationStepService.saveDatasetTransformationStep(datasetTransformationStep1)).thenReturn(datasetTransformationStep1);

        ResponseEntity<?> response = datasetTransformationStepController.createDatasetTransformationStep(datasetTransformationStep1);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(datasetTransformationStep1, response.getBody());
        verify(datasetTransformationStepService, times(1)).saveDatasetTransformationStep(datasetTransformationStep1);
    }

    /**
     * Tests the {@link DatasetTransformationStepController#createDatasetTransformationStep(DatasetTransformationStep)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testCreateDatasetTransformationStepFailure() {
        when(datasetTransformationStepService.saveDatasetTransformationStep(datasetTransformationStep1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = datasetTransformationStepController.createDatasetTransformationStep(datasetTransformationStep1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(datasetTransformationStepService, times(1)).saveDatasetTransformationStep(datasetTransformationStep1);
    }

    /**
     * Tests the {@link DatasetTransformationStepController#updateDatasetTransformationStep(Long, DatasetTransformationStep)} method.
     * Verifies that a datasetTransformationStep is updated successfully with a status of 200 OK when found.
     */
    @Test
    void testUpdateDatasetTransformationStepFound() {
        when(datasetTransformationStepService.updateDatasetTransformationStep(1L, datasetTransformationStep1)).thenReturn(Optional.of(datasetTransformationStep1));

        ResponseEntity<?> response = datasetTransformationStepController.updateDatasetTransformationStep(1L, datasetTransformationStep1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Optional.of(datasetTransformationStep1), response.getBody());
        verify(datasetTransformationStepService, times(1)).updateDatasetTransformationStep(1L, datasetTransformationStep1);
    }

    /**
     * Tests the {@link DatasetTransformationStepController#updateDatasetTransformationStep(Long, DatasetTransformationStep)} method.
     * Verifies that a status of 404 Not Found is returned when the datasetTransformationStep to update is not found.
     */
    @Test
    void testUpdateDatasetTransformationStepNotFound() {
        when(datasetTransformationStepService.updateDatasetTransformationStep(1L, datasetTransformationStep1)).thenReturn(Optional.empty());

        ResponseEntity<?> response = datasetTransformationStepController.updateDatasetTransformationStep(1L, datasetTransformationStep1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(datasetTransformationStepService, times(1)).updateDatasetTransformationStep(1L, datasetTransformationStep1);
    }

    /**
     * Tests the {@link DatasetTransformationStepController#updateDatasetTransformationStep(Long, DatasetTransformationStep)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testUpdateDatasetTransformationStepFailure() {
        when(datasetTransformationStepService.updateDatasetTransformationStep(1L, datasetTransformationStep1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = datasetTransformationStepController.updateDatasetTransformationStep(1L, datasetTransformationStep1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(datasetTransformationStepService, times(1)).updateDatasetTransformationStep(1L, datasetTransformationStep1);
    }

    /**
     * Tests the {@link DatasetTransformationStepController#deleteDatasetTransformationStep(Long)} method.
     * Verifies that a datasetTransformationStep is deleted successfully with a status of 204 No Content when found.
     */
    @Test
    void testDeleteDatasetTransformationStepFound() {
        when(datasetTransformationStepService.deleteDatasetTransformationStep(1L)).thenReturn(true);

        ResponseEntity<?> response = datasetTransformationStepController.deleteDatasetTransformationStep(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(datasetTransformationStepService, times(1)).deleteDatasetTransformationStep(1L);
    }

    /**
     * Tests the {@link DatasetTransformationStepController#deleteDatasetTransformationStep(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the datasetTransformationStep to delete is not found.
     */
    @Test
    void testDeleteDatasetTransformationStepNotFound() {
        when(datasetTransformationStepService.deleteDatasetTransformationStep(1L)).thenReturn(false);

        ResponseEntity<?> response = datasetTransformationStepController.deleteDatasetTransformationStep(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(datasetTransformationStepService, times(1)).deleteDatasetTransformationStep(1L);
    }

    /**
     * Tests the {@link DatasetTransformationStepController#deleteDatasetTransformationStep(Long)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testDeleteDatasetTransformationStepFailure() {
        when(datasetTransformationStepService.deleteDatasetTransformationStep(1L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = datasetTransformationStepController.deleteDatasetTransformationStep(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(datasetTransformationStepService, times(1)).deleteDatasetTransformationStep(1L);
    }
}
