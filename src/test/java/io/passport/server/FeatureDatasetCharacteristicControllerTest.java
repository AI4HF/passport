package io.passport.server;

import io.passport.server.controller.FeatureDatasetCharacteristicController;
import io.passport.server.model.FeatureDatasetCharacteristic;
import io.passport.server.model.FeatureDatasetCharacteristicDTO;
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
    private FeatureDatasetCharacteristicDTO featureDatasetCharacteristicDTO;
    
    /**
     * Sets up test data and initializes mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        featureDatasetCharacteristicId = new FeatureDatasetCharacteristicId(1L, 1L);
        featureDatasetCharacteristic1 = new FeatureDatasetCharacteristic(featureDatasetCharacteristicId, "Characteristic Name 1", "1", "Double");
        featureDatasetCharacteristic2 = new FeatureDatasetCharacteristic(featureDatasetCharacteristicId, "Characteristic Name 2", "1", "Double");
        featureDatasetCharacteristicDTO = new FeatureDatasetCharacteristicDTO(featureDatasetCharacteristic1);
    }

    /**
     * Tests the {@link FeatureDatasetCharacteristicController#getFeatureDatasetCharacteristics(Long, Long)} method.
     * Verifies that all featureDatasetCharacteristics are returned with a status of 200 OK.
     */
    @Test
    void testGetFeatureDatasetCharacteristicsNoParam() {
        when(featureDatasetCharacteristicService.getAllFeatureDatasetCharacteristics()).thenReturn(Arrays.asList(featureDatasetCharacteristic1, featureDatasetCharacteristic2));

        ResponseEntity<List<FeatureDatasetCharacteristicDTO>> response = featureDatasetCharacteristicController.getFeatureDatasetCharacteristics(null, null);

        HttpHeaders headers = response.getHeaders();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        assertEquals("2", headers.getFirst("X-Total-Count"));
        verify(featureDatasetCharacteristicService, times(1)).getAllFeatureDatasetCharacteristics();
    }

    /**
     * Tests the {@link FeatureDatasetCharacteristicController#getFeatureDatasetCharacteristics(Long, Long)} method.
     * Verifies that a featureDatasetCharacteristic list for the given datasetId is returned with a status of 200 OK when found.
     */
    @Test
    void testGetFeatureDatasetCharacteristicsWithDatasetId() {
        when(featureDatasetCharacteristicService.findByDatasetId(1L)).thenReturn(Arrays.asList(featureDatasetCharacteristic1, featureDatasetCharacteristic2));

        ResponseEntity<List<FeatureDatasetCharacteristicDTO>> response = featureDatasetCharacteristicController.getFeatureDatasetCharacteristics(1L, null);

        HttpHeaders headers = response.getHeaders();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        assertEquals("2", headers.getFirst("X-Total-Count"));
        verify(featureDatasetCharacteristicService, times(1)).findByDatasetId(1L);
    }

    /**
     * Tests the {@link FeatureDatasetCharacteristicController#getFeatureDatasetCharacteristics(Long, Long)} method.
     * Verifies that a featureDatasetCharacteristic list for the given featureId is returned with a status of 200 OK when found.
     */
    @Test
    void testGetFeatureDatasetCharacteristicsWithFeatureId() {
        when(featureDatasetCharacteristicService.findByFeatureId(1L)).thenReturn(Arrays.asList(featureDatasetCharacteristic1, featureDatasetCharacteristic2));

        ResponseEntity<List<FeatureDatasetCharacteristicDTO>> response = featureDatasetCharacteristicController.getFeatureDatasetCharacteristics(null, 1L);

        HttpHeaders headers = response.getHeaders();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        assertEquals("2", headers.getFirst("X-Total-Count"));
        verify(featureDatasetCharacteristicService, times(1)).findByFeatureId(1L);
    }

    /**
     * Tests the {@link FeatureDatasetCharacteristicController#getFeatureDatasetCharacteristics(Long, Long)} method.
     * Verifies that a featureDatasetCharacteristic for the given datasetId and featureId is returned with a status of 200 OK when found.
     */
    @Test
    void testGetFeatureDatasetCharacteristicsWithDatasetIdAndFeatureId() {
        when(featureDatasetCharacteristicService.findFeatureDatasetCharacteristicById(argThat(argument ->
                argument.getDatasetId().equals(1L) && argument.getFeatureId().equals(1L))))
                .thenReturn(Optional.of(featureDatasetCharacteristic1));

        ResponseEntity<List<FeatureDatasetCharacteristicDTO>> response = featureDatasetCharacteristicController.getFeatureDatasetCharacteristics(1L, 1L);

        HttpHeaders headers = response.getHeaders();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, Objects.requireNonNull(response.getBody()).size());
        assertEquals("1", headers.getFirst("X-Total-Count"));
        verify(featureDatasetCharacteristicService, times(1))
                .findFeatureDatasetCharacteristicById(argThat(argument ->
                        argument.getDatasetId().equals(1L) && argument.getFeatureId().equals(1L)));
    }

    /**
     * Tests the {@link FeatureDatasetCharacteristicController#createFeatureDatasetCharacteristic(FeatureDatasetCharacteristicDTO)} method.
     * Verifies that a featureDatasetCharacteristic is created successfully with a status of 201 Created.
     */
    @Test
    void testCreateFeatureDatasetCharacteristicSuccess() {
        when(featureDatasetCharacteristicService.saveFeatureDatasetCharacteristic(argThat(argument ->
                argument.getId().getDatasetId().equals(1L) && argument.getId().getFeatureId().equals(1L))))
                .thenReturn(featureDatasetCharacteristic1);

        ResponseEntity<?> response = featureDatasetCharacteristicController.createFeatureDatasetCharacteristic(featureDatasetCharacteristicDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(featureDatasetCharacteristic1, response.getBody());
        verify(featureDatasetCharacteristicService, times(1)).saveFeatureDatasetCharacteristic(argThat(argument ->
                argument.getId().getDatasetId().equals(1L) && argument.getId().getFeatureId().equals(1L)));
    }

    /**
     * Tests the {@link FeatureDatasetCharacteristicController#createFeatureDatasetCharacteristic(FeatureDatasetCharacteristicDTO)}  method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testCreateFeatureDatasetCharacteristicFailure() {
        when(featureDatasetCharacteristicService.saveFeatureDatasetCharacteristic(argThat(argument ->
                argument.getId().getDatasetId().equals(1L) && argument.getId().getFeatureId().equals(1L))))
                .thenThrow(new RuntimeException("error"));

        ResponseEntity<?> response = featureDatasetCharacteristicController.createFeatureDatasetCharacteristic(featureDatasetCharacteristicDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(featureDatasetCharacteristicService, times(1)).saveFeatureDatasetCharacteristic(argThat(argument ->
                argument.getId().getDatasetId().equals(1L) && argument.getId().getFeatureId().equals(1L)));
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
