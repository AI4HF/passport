package io.passport.server;

import io.passport.server.controller.DatasetTransformationController;
import io.passport.server.model.DatasetTransformation;
import io.passport.server.service.DatasetTransformationService;
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
 * Unit tests for {@link DatasetTransformationController}.
 */
public class DatasetTransformationControllerTest {
    @Mock
    private DatasetTransformationService datasetTransformationService;

    @InjectMocks
    private DatasetTransformationController datasetTransformationController;

    private DatasetTransformation datasetTransformation1;
    private DatasetTransformation datasetTransformation2;

    /**
     * Sets up test data and initializes mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        datasetTransformation1 = new DatasetTransformation(1L,"Title 1", "Description 1");
        datasetTransformation2 = new DatasetTransformation(2L, "Title 2", "Description 2");
    }

    /**
     * Tests the {@link DatasetTransformationController#getAllDatasetTransformations()} method.
     * Verifies that all datasetTransformations are returned with a status of 200 OK.
     */
    @Test
    void testGetAllDatasetTransformations() {
        when(datasetTransformationService.getAllDatasetTransformations()).thenReturn(Arrays.asList(datasetTransformation1, datasetTransformation2));

        ResponseEntity<List<DatasetTransformation>> response = datasetTransformationController.getAllDatasetTransformations();

        HttpHeaders headers = response.getHeaders();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        assertEquals("2", headers.getFirst("X-Total-Count"));
        verify(datasetTransformationService, times(1)).getAllDatasetTransformations();
    }

    /**
     * Tests the {@link DatasetTransformationController#getDatasetTransformation(Long)} method.
     * Verifies that a datasetTransformation is returned with a status of 200 OK when found.
     */
    @Test
    void testGetDatasetTransformationFound() {
        when(datasetTransformationService.findDatasetTransformationByDataTransformationId(1L)).thenReturn(Optional.of(datasetTransformation1));

        ResponseEntity<?> response = datasetTransformationController.getDatasetTransformation(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(datasetTransformation1, response.getBody());
        verify(datasetTransformationService, times(1)).findDatasetTransformationByDataTransformationId(1L);
    }

    /**
     * Tests the {@link DatasetTransformationController#getDatasetTransformation(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the datasetTransformation is not found.
     */
    @Test
    void testGetDatasetTransformationNotFound() {
        when(datasetTransformationService.findDatasetTransformationByDataTransformationId(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = datasetTransformationController.getDatasetTransformation(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(datasetTransformationService, times(1)).findDatasetTransformationByDataTransformationId(1L);
    }

    /**
     * Tests the {@link DatasetTransformationController#createDatasetTransformation(DatasetTransformation)} method.
     * Verifies that a datasetTransformation is created successfully with a status of 201 Created.
     */
    @Test
    void testCreateDatasetTransformationSuccess() {
        when(datasetTransformationService.saveDatasetTransformation(datasetTransformation1)).thenReturn(datasetTransformation1);

        ResponseEntity<?> response = datasetTransformationController.createDatasetTransformation(datasetTransformation1);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(datasetTransformation1, response.getBody());
        verify(datasetTransformationService, times(1)).saveDatasetTransformation(datasetTransformation1);
    }

    /**
     * Tests the {@link DatasetTransformationController#createDatasetTransformation(DatasetTransformation)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testCreateDatasetTransformationFailure() {
        when(datasetTransformationService.saveDatasetTransformation(datasetTransformation1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = datasetTransformationController.createDatasetTransformation(datasetTransformation1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(datasetTransformationService, times(1)).saveDatasetTransformation(datasetTransformation1);
    }

    /**
     * Tests the {@link DatasetTransformationController#updateDatasetTransformation(Long, DatasetTransformation)} method.
     * Verifies that a datasetTransformation is updated successfully with a status of 200 OK when found.
     */
    @Test
    void testUpdateDatasetTransformationFound() {
        when(datasetTransformationService.updateDatasetTransformation(1L, datasetTransformation1)).thenReturn(Optional.of(datasetTransformation1));

        ResponseEntity<?> response = datasetTransformationController.updateDatasetTransformation(1L, datasetTransformation1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Optional.of(datasetTransformation1), response.getBody());
        verify(datasetTransformationService, times(1)).updateDatasetTransformation(1L, datasetTransformation1);
    }

    /**
     * Tests the {@link DatasetTransformationController#updateDatasetTransformation(Long, DatasetTransformation)} method.
     * Verifies that a status of 404 Not Found is returned when the datasetTransformation to update is not found.
     */
    @Test
    void testUpdateDatasetTransformationNotFound() {
        when(datasetTransformationService.updateDatasetTransformation(1L, datasetTransformation1)).thenReturn(Optional.empty());

        ResponseEntity<?> response = datasetTransformationController.updateDatasetTransformation(1L, datasetTransformation1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(datasetTransformationService, times(1)).updateDatasetTransformation(1L, datasetTransformation1);
    }

    /**
     * Tests the {@link DatasetTransformationController#updateDatasetTransformation(Long, DatasetTransformation)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testUpdateDatasetTransformationFailure() {
        when(datasetTransformationService.updateDatasetTransformation(1L, datasetTransformation1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = datasetTransformationController.updateDatasetTransformation(1L, datasetTransformation1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(datasetTransformationService, times(1)).updateDatasetTransformation(1L, datasetTransformation1);
    }

    /**
     * Tests the {@link DatasetTransformationController#deleteDatasetTransformation(Long)} method.
     * Verifies that a datasetTransformation is deleted successfully with a status of 204 No Content when found.
     */
    @Test
    void testDeleteDatasetTransformationFound() {
        when(datasetTransformationService.deleteDatasetTransformation(1L)).thenReturn(true);

        ResponseEntity<?> response = datasetTransformationController.deleteDatasetTransformation(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(datasetTransformationService, times(1)).deleteDatasetTransformation(1L);
    }

    /**
     * Tests the {@link DatasetTransformationController#deleteDatasetTransformation(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the datasetTransformation to delete is not found.
     */
    @Test
    void testDeleteDatasetTransformationNotFound() {
        when(datasetTransformationService.deleteDatasetTransformation(1L)).thenReturn(false);

        ResponseEntity<?> response = datasetTransformationController.deleteDatasetTransformation(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(datasetTransformationService, times(1)).deleteDatasetTransformation(1L);
    }

    /**
     * Tests the {@link DatasetTransformationController#deleteDatasetTransformation(Long)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testDeleteDatasetTransformationFailure() {
        when(datasetTransformationService.deleteDatasetTransformation(1L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = datasetTransformationController.deleteDatasetTransformation(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(datasetTransformationService, times(1)).deleteDatasetTransformation(1L);
    }
}
