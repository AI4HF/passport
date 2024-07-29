package io.passport.server;

import io.passport.server.controller.FeatureDatasetCharacteristicController;
import io.passport.server.model.FeatureDatasetCharacteristic;
import io.passport.server.model.FeatureDatasetCharacteristicId;
import io.passport.server.service.FeatureDatasetCharacteristicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link FeatureDatasetCharacteristicController}.
 */
public class FeatureDatasetCharacteristicControllerTest {
    @Mock
    private FeatureDatasetCharacteristicService featureDatasetCharacteristicService;

    @InjectMocks
    private FeatureDatasetCharacteristicController featureDatasetCharacteristicController;

    private FeatureDatasetCharacteristicId featureDatasetCharacteristicId;
    private FeatureDatasetCharacteristic featureDatasetCharacteristic1;
    private FeatureDatasetCharacteristic featureDatasetCharacteristic2;
    
    /**
     * Sets up test data and initializes mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        featureDatasetCharacteristicId = new FeatureDatasetCharacteristicId(1L, 1L);
        featureDatasetCharacteristic1 = new FeatureDatasetCharacteristic(featureDatasetCharacteristicId, "Characteristic Name 1", 10.0, "Double");
        featureDatasetCharacteristic2 = new FeatureDatasetCharacteristic(featureDatasetCharacteristicId, "Characteristic Name 2", 20.0, "Double");
    }

    /**
     * Tests the {@link FeatureDatasetCharacteristicController#getAllFeatureDatasetCharacteristics()} method.
     * Verifies that all featureDatasetCharacteristics are returned with a status of 200 OK.
     */
    @Test
    void testGetAllFeatureDatasetCharacteristics() {
        when(featureDatasetCharacteristicService.getAllFeatureDatasetCharacteristics()).thenReturn(Arrays.asList(featureDatasetCharacteristic1, featureDatasetCharacteristic2));

        ResponseEntity<List<FeatureDatasetCharacteristic>> response = featureDatasetCharacteristicController.getAllFeatureDatasetCharacteristics();

        HttpHeaders headers = response.getHeaders();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        assertEquals("2", headers.getFirst("X-Total-Count"));
        verify(featureDatasetCharacteristicService, times(1)).getAllFeatureDatasetCharacteristics();
    }

    /**
     * Tests the {@link FeatureDatasetCharacteristicController#getFeatureDatasetCharacteristicsByDatasetId(Long)} method.
     * Verifies that a featureDatasetCharacteristic list for the given datasetId is returned with a status of 200 OK when found.
     */
    @Test
    void testGetFeatureDatasetCharacteristicsByDatasetId() {
        when(featureDatasetCharacteristicService.findByDatasetId(1L)).thenReturn(Arrays.asList(featureDatasetCharacteristic1, featureDatasetCharacteristic2));

        ResponseEntity<List<FeatureDatasetCharacteristic>> response = featureDatasetCharacteristicController.getFeatureDatasetCharacteristicsByDatasetId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        verify(featureDatasetCharacteristicService, times(1)).findByDatasetId(1L);
    }

    /**
     * Tests the {@link FeatureDatasetCharacteristicController#getFeatureDatasetCharacteristicsByFeatureId(Long)} method.
     * Verifies that a featureDatasetCharacteristic list for the given featureId is returned with a status of 200 OK when found.
     */
    @Test
    void testGetFeatureDatasetCharacteristicsByFeatureId() {
        when(featureDatasetCharacteristicService.findByFeatureId(1L)).thenReturn(Arrays.asList(featureDatasetCharacteristic1, featureDatasetCharacteristic2));

        ResponseEntity<List<FeatureDatasetCharacteristic>> response = featureDatasetCharacteristicController.getFeatureDatasetCharacteristicsByFeatureId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        verify(featureDatasetCharacteristicService, times(1)).findByFeatureId(1L);
    }

    /**
     * Tests the {@link FeatureDatasetCharacteristicController#getFeatureDatasetCharacteristic(Long, Long)} method.
     * Verifies that a featureDatasetCharacteristic is returned for the given datasetId and featureId with a status of 200 OK when found.
     */
    @Test
    void testGetFeatureDatasetCharacteristicFound() {
        when(featureDatasetCharacteristicService.findFeatureDatasetCharacteristicById(argThat(id ->
                id.getDatasetId().equals(1L) && id.getFeatureId().equals(1L))))
                .thenReturn(Optional.of(featureDatasetCharacteristic1));

        ResponseEntity<?> response = featureDatasetCharacteristicController.getFeatureDatasetCharacteristic(1L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(featureDatasetCharacteristic1, response.getBody());
        verify(featureDatasetCharacteristicService, times(1))
                .findFeatureDatasetCharacteristicById(argThat(id ->
                        id.getDatasetId().equals(1L) && id.getFeatureId().equals(1L)));
    }

    /**
     * Tests the {@link FeatureDatasetCharacteristicController#getFeatureDatasetCharacteristic(Long, Long)} method.
     * Verifies that a status of 404 Not Found is returned when featureDatasetCharacteristic is not found.
     */
    @Test
    void testGetFeatureDatasetCharacteristicNotFound() {
        when(featureDatasetCharacteristicService.findFeatureDatasetCharacteristicById(featureDatasetCharacteristicId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = featureDatasetCharacteristicController.getFeatureDatasetCharacteristic(1L, 1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(featureDatasetCharacteristicService, times(1))
                .findFeatureDatasetCharacteristicById(argThat(id ->
                        id.getDatasetId().equals(1L) && id.getFeatureId().equals(1L)));
    }

    /**
     * Tests the {@link FeatureDatasetCharacteristicController#createFeatureDatasetCharacteristic(FeatureDatasetCharacteristic)} method.
     * Verifies that a featureDatasetCharacteristic is created successfully with a status of 201 Created.
     */
    @Test
    void testCreateFeatureDatasetCharacteristicSuccess() {
        when(featureDatasetCharacteristicService.saveFeatureDatasetCharacteristic(featureDatasetCharacteristic1)).thenReturn(featureDatasetCharacteristic1);

        ResponseEntity<?> response = featureDatasetCharacteristicController.createFeatureDatasetCharacteristic(featureDatasetCharacteristic1);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(featureDatasetCharacteristic1, response.getBody());
        verify(featureDatasetCharacteristicService, times(1)).saveFeatureDatasetCharacteristic(featureDatasetCharacteristic1);
    }

    /**
     * Tests the {@link FeatureDatasetCharacteristicController#createFeatureDatasetCharacteristic(FeatureDatasetCharacteristic)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testCreateFeatureDatasetCharacteristicFailure() {
        when(featureDatasetCharacteristicService.saveFeatureDatasetCharacteristic(featureDatasetCharacteristic1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = featureDatasetCharacteristicController.createFeatureDatasetCharacteristic(featureDatasetCharacteristic1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(featureDatasetCharacteristicService, times(1)).saveFeatureDatasetCharacteristic(featureDatasetCharacteristic1);
    }

    /**
     * Tests the {@link FeatureDatasetCharacteristicController#updateFeatureDatasetCharacteristic(Long, Long, FeatureDatasetCharacteristic)} method.
     * Verifies that a featureDatasetCharacteristic is updated successfully with a status of 200 OK when found.
     */
    @Test
    void testUpdateFeatureDatasetCharacteristicFound() {
        when(featureDatasetCharacteristicService.updateFeatureDatasetCharacteristic(argThat(id ->
                id.getDatasetId().equals(1L) && id.getFeatureId().equals(1L)), eq(featureDatasetCharacteristic1)))
                .thenReturn(Optional.of(featureDatasetCharacteristic1));

        ResponseEntity<?> response = featureDatasetCharacteristicController.updateFeatureDatasetCharacteristic(1L, 1L, featureDatasetCharacteristic1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Optional.of(featureDatasetCharacteristic1), response.getBody());
        verify(featureDatasetCharacteristicService, times(1))
                .updateFeatureDatasetCharacteristic(argThat(id ->
                        id.getDatasetId().equals(1L) && id.getFeatureId().equals(1L)), eq(featureDatasetCharacteristic1));
    }

    /**
     * Tests the {@link FeatureDatasetCharacteristicController#updateFeatureDatasetCharacteristic(Long, Long, FeatureDatasetCharacteristic)}  method.
     * Verifies that a status of 404 Not Found is returned when the featureDatasetCharacteristic to update is not found.
     */
    @Test
    void testUpdateFeatureDatasetCharacteristicNotFound() {
        when(featureDatasetCharacteristicService.updateFeatureDatasetCharacteristic(argThat(id ->
                id.getDatasetId().equals(1L) && id.getFeatureId().equals(1L)), eq(featureDatasetCharacteristic1)))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response = featureDatasetCharacteristicController.updateFeatureDatasetCharacteristic(1L, 1L, featureDatasetCharacteristic1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(featureDatasetCharacteristicService, times(1))
                .updateFeatureDatasetCharacteristic(argThat(id ->
                        id.getDatasetId().equals(1L) && id.getFeatureId().equals(1L)), eq(featureDatasetCharacteristic1));
    }

    /**
     * Tests the {@link FeatureDatasetCharacteristicController#updateFeatureDatasetCharacteristic(Long, Long, FeatureDatasetCharacteristic)}  method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testUpdateFeatureDatasetCharacteristicFailure() {
        when(featureDatasetCharacteristicService.updateFeatureDatasetCharacteristic(argThat(id ->
                id.getDatasetId().equals(1L) && id.getFeatureId().equals(1L)), eq(featureDatasetCharacteristic1)))
                .thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = featureDatasetCharacteristicController.updateFeatureDatasetCharacteristic(1L, 1L, featureDatasetCharacteristic1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(featureDatasetCharacteristicService, times(1))
                .updateFeatureDatasetCharacteristic(argThat(id ->
                        id.getDatasetId().equals(1L) && id.getFeatureId().equals(1L)), eq(featureDatasetCharacteristic1));
    }

    /**
     * Tests the {@link FeatureDatasetCharacteristicController#deleteFeatureDatasetCharacteristic(Long, Long)} method.
     * Verifies that a featureDatasetCharacteristic is deleted successfully with a status of 204 No Content when found.
     */
    @Test
    void testDeleteFeatureDatasetCharacteristicFound() {
        when(featureDatasetCharacteristicService.deleteFeatureDatasetCharacteristic(argThat(id ->
                id.getDatasetId().equals(1L) && id.getFeatureId().equals(1L))))
                .thenReturn(true);

        ResponseEntity<?> response = featureDatasetCharacteristicController.deleteFeatureDatasetCharacteristic(1L, 1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(featureDatasetCharacteristicService, times(1))
                .deleteFeatureDatasetCharacteristic(argThat(id ->
                        id.getDatasetId().equals(1L) && id.getFeatureId().equals(1L)));
    }

    /**
     * Tests the {@link FeatureDatasetCharacteristicController#deleteFeatureDatasetCharacteristic(Long, Long)} method.
     * Verifies that a status of 404 Not Found is returned when the featureDatasetCharacteristic to delete is not found.
     */
    @Test
    void testDeleteFeatureDatasetCharacteristicNotFound() {
        when(featureDatasetCharacteristicService.deleteFeatureDatasetCharacteristic(argThat(id ->
                id.getDatasetId().equals(1L) && id.getFeatureId().equals(1L))))
                .thenReturn(false);

        ResponseEntity<?> response = featureDatasetCharacteristicController.deleteFeatureDatasetCharacteristic(1L, 1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(featureDatasetCharacteristicService, times(1))
                .deleteFeatureDatasetCharacteristic(argThat(id ->
                        id.getDatasetId().equals(1L) && id.getFeatureId().equals(1L)));
    }

    /**
     * Tests the {@link FeatureDatasetCharacteristicController#deleteFeatureDatasetCharacteristic(Long, Long)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testDeleteFeatureDatasetCharacteristicFailure() {
        when(featureDatasetCharacteristicService.deleteFeatureDatasetCharacteristic(argThat(id ->
                id.getDatasetId().equals(1L) && id.getFeatureId().equals(1L))))
                .thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = featureDatasetCharacteristicController.deleteFeatureDatasetCharacteristic(1L, 1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(featureDatasetCharacteristicService, times(1))
                .deleteFeatureDatasetCharacteristic(argThat(id ->
                        id.getDatasetId().equals(1L) && id.getFeatureId().equals(1L)));
    }
}
