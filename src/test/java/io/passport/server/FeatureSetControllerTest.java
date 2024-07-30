package io.passport.server;

import io.passport.server.controller.FeatureSetController;
import io.passport.server.model.FeatureSet;
import io.passport.server.service.FeatureSetService;
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
 * Unit tests for {@link FeatureSetController}.
 */
public class FeatureSetControllerTest {
    @Mock
    private FeatureSetService featureSetService;

    @InjectMocks
    private FeatureSetController featureSetController;

    private FeatureSet featureSet1;
    private FeatureSet featureSet2;

    /**
     * Sets up test data and initializes mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        featureSet1 = new FeatureSet(1L, 1L, "FeatureSet Title 1", "http://featureset1.url", "FeatureSet Description 1", Instant.now(), "1", Instant.now(), "1");
        featureSet2 = new FeatureSet(2L, 1L, "FeatureSet Title 2", "http://featureset2.url", "FeatureSet Description 2", Instant.now(), "1", Instant.now(), "1");
    }

    /**
     * Tests the {@link FeatureSetController#getAllFeatureSets()} method.
     * Verifies that all featureSets are returned with a status of 200 OK.
     */
    @Test
    void testGetAllFeatureSets() {
        when(featureSetService.getAllFeatureSets()).thenReturn(Arrays.asList(featureSet1, featureSet2));

        ResponseEntity<List<FeatureSet>> response = featureSetController.getAllFeatureSets();

        HttpHeaders headers = response.getHeaders();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        assertEquals("2", headers.getFirst("X-Total-Count"));
        verify(featureSetService, times(1)).getAllFeatureSets();
    }

    /**
     * Tests the {@link FeatureSetController#getFeatureSet(Long)} method.
     * Verifies that a featureSet is returned with a status of 200 OK when found.
     */
    @Test
    void testGetFeatureSetFound() {
        when(featureSetService.findFeatureSetByFeatureSetId(1L)).thenReturn(Optional.of(featureSet1));

        ResponseEntity<?> response = featureSetController.getFeatureSet(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(featureSet1, response.getBody());
        verify(featureSetService, times(1)).findFeatureSetByFeatureSetId(1L);
    }

    /**
     * Tests the {@link FeatureSetController#getFeatureSet(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the featureSet is not found.
     */
    @Test
    void testGetFeatureSetNotFound() {
        when(featureSetService.findFeatureSetByFeatureSetId(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = featureSetController.getFeatureSet(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(featureSetService, times(1)).findFeatureSetByFeatureSetId(1L);
    }

    /**
     * Tests the {@link FeatureSetController#createFeatureSet(FeatureSet)} method.
     * Verifies that a featureSet is created successfully with a status of 201 Created.
     */
    @Test
    void testCreateFeatureSetSuccess() {
        when(featureSetService.saveFeatureSet(featureSet1)).thenReturn(featureSet1);

        ResponseEntity<?> response = featureSetController.createFeatureSet(featureSet1);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(featureSet1, response.getBody());
        verify(featureSetService, times(1)).saveFeatureSet(featureSet1);
    }

    /**
     * Tests the {@link FeatureSetController#createFeatureSet(FeatureSet)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testCreateFeatureSetFailure() {
        when(featureSetService.saveFeatureSet(featureSet1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = featureSetController.createFeatureSet(featureSet1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(featureSetService, times(1)).saveFeatureSet(featureSet1);
    }

    /**
     * Tests the {@link FeatureSetController#updateFeatureSet(Long, FeatureSet)} method.
     * Verifies that a featureSet is updated successfully with a status of 200 OK when found.
     */
    @Test
    void testUpdateFeatureSetFound() {
        when(featureSetService.updateFeatureSet(1L, featureSet1)).thenReturn(Optional.of(featureSet1));

        ResponseEntity<?> response = featureSetController.updateFeatureSet(1L, featureSet1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Optional.of(featureSet1), response.getBody());
        verify(featureSetService, times(1)).updateFeatureSet(1L, featureSet1);
    }

    /**
     * Tests the {@link FeatureSetController#updateFeatureSet(Long, FeatureSet)} method.
     * Verifies that a status of 404 Not Found is returned when the featureSet to update is not found.
     */
    @Test
    void testUpdateFeatureSetNotFound() {
        when(featureSetService.updateFeatureSet(1L, featureSet1)).thenReturn(Optional.empty());

        ResponseEntity<?> response = featureSetController.updateFeatureSet(1L, featureSet1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(featureSetService, times(1)).updateFeatureSet(1L, featureSet1);
    }

    /**
     * Tests the {@link FeatureSetController#updateFeatureSet(Long, FeatureSet)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testUpdateFeatureSetFailure() {
        when(featureSetService.updateFeatureSet(1L, featureSet1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = featureSetController.updateFeatureSet(1L, featureSet1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(featureSetService, times(1)).updateFeatureSet(1L, featureSet1);
    }

    /**
     * Tests the {@link FeatureSetController#deleteFeatureSet(Long)} method.
     * Verifies that a featureSet is deleted successfully with a status of 204 No Content when found.
     */
    @Test
    void testDeleteFeatureSetFound() {
        when(featureSetService.deleteFeatureSet(1L)).thenReturn(true);

        ResponseEntity<?> response = featureSetController.deleteFeatureSet(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(featureSetService, times(1)).deleteFeatureSet(1L);
    }

    /**
     * Tests the {@link FeatureSetController#deleteFeatureSet(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the featureSet to delete is not found.
     */
    @Test
    void testDeleteFeatureSetNotFound() {
        when(featureSetService.deleteFeatureSet(1L)).thenReturn(false);

        ResponseEntity<?> response = featureSetController.deleteFeatureSet(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(featureSetService, times(1)).deleteFeatureSet(1L);
    }

    /**
     * Tests the {@link FeatureSetController#deleteFeatureSet(Long)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testDeleteFeatureSetFailure() {
        when(featureSetService.deleteFeatureSet(1L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = featureSetController.deleteFeatureSet(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(featureSetService, times(1)).deleteFeatureSet(1L);
    }
}
