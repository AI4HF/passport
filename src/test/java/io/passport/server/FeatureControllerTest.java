package io.passport.server;

import io.passport.server.controller.FeatureController;
import io.passport.server.model.Feature;
import io.passport.server.service.FeatureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link FeatureController}.
 */
public class FeatureControllerTest {
    @Mock
    private FeatureService featureService;

    @InjectMocks
    private FeatureController featureController;

    private Feature feature1;
    private Feature feature2;

    /**
     * Sets up test data and initializes mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        feature1 = new Feature(1L, 1L, "Feature Title 1", "Feature Description 1", "String", "Type 1", true, false, "Units 1", "Equipment 1", "Data Collection 1", Instant.now(), "1", Instant.now(), "1");
        feature2 = new Feature(2L, 1L, "Feature Title 2", "Feature Description 2", "String", "Type 2", false, true, "Units 2", "Equipment 2", "Data Collection 2", Instant.now(), "1", Instant.now(), "1");
    }

    /**
     * Tests the {@link FeatureController#getFeatures(Long)} method.
     * Verifies that all features are returned with a status of 200 OK.
     */
    @Test
    void testGetFeaturesNoParam() {
        when(featureService.getAllFeatures()).thenReturn(Arrays.asList(feature1, feature2));

        ResponseEntity<List<Feature>> response = featureController.getFeatures(null);

        HttpHeaders headers = response.getHeaders();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        assertEquals("2", headers.getFirst("X-Total-Count"));
        verify(featureService, times(1)).getAllFeatures();
    }

    /**
     * Tests the {@link FeatureController#getFeatures(Long)}  method.
     * Verifies that all features with given featureSetId are returned with a status of 200 OK.
     */
    @Test
    void testGetFeaturesWithParam() {
        when(featureService.findByFeaturesetId(1L)).thenReturn(Arrays.asList(feature1, feature2));

        ResponseEntity<List<Feature>> response = featureController.getFeatures(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        verify(featureService, times(1)).findByFeaturesetId(1L);
    }

    /**
     * Tests the {@link FeatureController#getFeature(Long)} method.
     * Verifies that a feature is returned with a status of 200 OK when found.
     */
    @Test
    void testGetFeatureFound() {
        when(featureService.findFeatureByFeatureId(1L)).thenReturn(Optional.of(feature1));

        ResponseEntity<?> response = featureController.getFeature(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(feature1, response.getBody());
        verify(featureService, times(1)).findFeatureByFeatureId(1L);
    }

    /**
     * Tests the {@link FeatureController#getFeature(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the feature is not found.
     */
    @Test
    void testGetFeatureNotFound() {
        when(featureService.findFeatureByFeatureId(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = featureController.getFeature(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(featureService, times(1)).findFeatureByFeatureId(1L);
    }

    /**
     * Tests the {@link FeatureController#createFeature(Feature)} method.
     * Verifies that a feature is created successfully with a status of 201 Created.
     */
    @Test
    void testCreateFeatureSuccess() {
        when(featureService.saveFeature(feature1)).thenReturn(feature1);

        ResponseEntity<?> response = featureController.createFeature(feature1);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(feature1, response.getBody());
        verify(featureService, times(1)).saveFeature(feature1);
    }

    /**
     * Tests the {@link FeatureController#createFeature(Feature)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testCreateFeatureFailure() {
        when(featureService.saveFeature(feature1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = featureController.createFeature(feature1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(featureService, times(1)).saveFeature(feature1);
    }

    /**
     * Tests the {@link FeatureController#updateFeature(Long, Feature)} method.
     * Verifies that a feature is updated successfully with a status of 200 OK when found.
     */
    @Test
    void testUpdateFeatureFound() {
        when(featureService.updateFeature(1L, feature1)).thenReturn(Optional.of(feature1));

        ResponseEntity<?> response = featureController.updateFeature(1L, feature1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Optional.of(feature1), response.getBody());
        verify(featureService, times(1)).updateFeature(1L, feature1);
    }

    /**
     * Tests the {@link FeatureController#updateFeature(Long, Feature)} method.
     * Verifies that a status of 404 Not Found is returned when the feature to update is not found.
     */
    @Test
    void testUpdateFeatureNotFound() {
        when(featureService.updateFeature(1L, feature1)).thenReturn(Optional.empty());

        ResponseEntity<?> response = featureController.updateFeature(1L, feature1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(featureService, times(1)).updateFeature(1L, feature1);
    }

    /**
     * Tests the {@link FeatureController#updateFeature(Long, Feature)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testUpdateFeatureFailure() {
        when(featureService.updateFeature(1L, feature1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = featureController.updateFeature(1L, feature1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(featureService, times(1)).updateFeature(1L, feature1);
    }

    /**
     * Tests the {@link FeatureController#deleteFeature(Long)} method.
     * Verifies that a feature is deleted successfully with a status of 204 No Content when found.
     */
    @Test
    void testDeleteFeatureFound() {
        when(featureService.deleteFeature(1L)).thenReturn(true);

        ResponseEntity<?> response = featureController.deleteFeature(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(featureService, times(1)).deleteFeature(1L);
    }

    /**
     * Tests the {@link FeatureController#deleteFeature(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the feature to delete is not found.
     */
    @Test
    void testDeleteFeatureNotFound() {
        when(featureService.deleteFeature(1L)).thenReturn(false);

        ResponseEntity<?> response = featureController.deleteFeature(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(featureService, times(1)).deleteFeature(1L);
    }

    /**
     * Tests the {@link FeatureController#deleteFeature(Long)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testDeleteFeatureFailure() {
        when(featureService.deleteFeature(1L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = featureController.deleteFeature(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(featureService, times(1)).deleteFeature(1L);
    }
}
