package io.passport.server;

import io.passport.server.controller.DatasetController;
import io.passport.server.model.Dataset;
import io.passport.server.service.DatasetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DatasetController}.
 */
class DatasetControllerTest {

    @Mock
    private DatasetService datasetService;

    @InjectMocks
    private DatasetController datasetController;

    private Dataset dataset1;
    private Dataset dataset2;

    /**
     * Sets up test data and initializes mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dataset1 = new Dataset(1L, 1L, 1L, 1L, "Title 1", "Description 1", "Version 1", "Ref Entity 1", 100, true, LocalDateTime.now(), 1L, LocalDateTime.now(), 1L);
        dataset2 = new Dataset(2L, 1L, 1L, 1L, "Title 2", "Description 2", "Version 2", "Ref Entity 2", 100, true, LocalDateTime.now(), 1L, LocalDateTime.now(), 1L);
    }

    /**
     * Tests the {@link DatasetController#getAllDatasets()} method.
     * Verifies that all datasets are returned with a status of 200 OK.
     */
    @Test
    void testGetAllDatasets() {
        when(datasetService.getAllDatasets()).thenReturn(Arrays.asList(dataset1, dataset2));

        ResponseEntity<List<Dataset>> response = datasetController.getAllDatasets();

        HttpHeaders headers = response.getHeaders();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        assertEquals("2", headers.getFirst("X-Total-Count"));
        verify(datasetService, times(1)).getAllDatasets();
    }

    /**
     * Tests the {@link DatasetController#getDataset(Long)} method.
     * Verifies that a dataset is returned with a status of 200 OK when found.
     */
    @Test
    void testGetDatasetFound() {
        when(datasetService.findDatasetByDatasetId(1L)).thenReturn(Optional.of(dataset1));

        ResponseEntity<?> response = datasetController.getDataset(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dataset1, response.getBody());
        verify(datasetService, times(1)).findDatasetByDatasetId(1L);
    }

    /**
     * Tests the {@link DatasetController#getDataset(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the dataset is not found.
     */
    @Test
    void testGetDatasetNotFound() {
        when(datasetService.findDatasetByDatasetId(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = datasetController.getDataset(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(datasetService, times(1)).findDatasetByDatasetId(1L);
    }

    /**
     * Tests the {@link DatasetController#createDataset(Dataset)} method.
     * Verifies that a dataset is created successfully with a status of 201 Created.
     */
    @Test
    void testCreateDatasetSuccess() {
        when(datasetService.saveDataset(dataset1)).thenReturn(dataset1);

        ResponseEntity<?> response = datasetController.createDataset(dataset1);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(dataset1, response.getBody());
        verify(datasetService, times(1)).saveDataset(dataset1);
    }

    /**
     * Tests the {@link DatasetController#createDataset(Dataset)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testCreateDatasetFailure() {
        when(datasetService.saveDataset(dataset1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = datasetController.createDataset(dataset1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(datasetService, times(1)).saveDataset(dataset1);
    }

    /**
     * Tests the {@link DatasetController#updateDataset(Long, Dataset)} method.
     * Verifies that a dataset is updated successfully with a status of 200 OK when found.
     */
    @Test
    void testUpdateDatasetFound() {
        when(datasetService.updateDataset(1L, dataset1)).thenReturn(Optional.of(dataset1));

        ResponseEntity<?> response = datasetController.updateDataset(1L, dataset1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Optional.of(dataset1), response.getBody());
        verify(datasetService, times(1)).updateDataset(1L, dataset1);
    }

    /**
     * Tests the {@link DatasetController#updateDataset(Long, Dataset)} method.
     * Verifies that a status of 404 Not Found is returned when the dataset to update is not found.
     */
    @Test
    void testUpdateDatasetNotFound() {
        when(datasetService.updateDataset(1L, dataset1)).thenReturn(Optional.empty());

        ResponseEntity<?> response = datasetController.updateDataset(1L, dataset1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(datasetService, times(1)).updateDataset(1L, dataset1);
    }

    /**
     * Tests the {@link DatasetController#updateDataset(Long, Dataset)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testUpdateDatasetFailure() {
        when(datasetService.updateDataset(1L, dataset1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = datasetController.updateDataset(1L, dataset1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(datasetService, times(1)).updateDataset(1L, dataset1);
    }

    /**
     * Tests the {@link DatasetController#deleteDataset(Long)} method.
     * Verifies that a dataset is deleted successfully with a status of 204 No Content when found.
     */
    @Test
    void testDeleteDatasetFound() {
        when(datasetService.deleteDataset(1L)).thenReturn(true);

        ResponseEntity<?> response = datasetController.deleteDataset(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(datasetService, times(1)).deleteDataset(1L);
    }

    /**
     * Tests the {@link DatasetController#deleteDataset(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the dataset to delete is not found.
     */
    @Test
    void testDeleteDatasetNotFound() {
        when(datasetService.deleteDataset(1L)).thenReturn(false);

        ResponseEntity<?> response = datasetController.deleteDataset(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(datasetService, times(1)).deleteDataset(1L);
    }

    /**
     * Tests the {@link DatasetController#deleteDataset(Long)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testDeleteDatasetFailure() {
        when(datasetService.deleteDataset(1L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = datasetController.deleteDataset(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(datasetService, times(1)).deleteDataset(1L);
    }
}
