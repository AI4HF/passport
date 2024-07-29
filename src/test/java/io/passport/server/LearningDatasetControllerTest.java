package io.passport.server;

import io.passport.server.controller.LearningDatasetController;
import io.passport.server.model.LearningDataset;
import io.passport.server.service.LearningDatasetService;
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
 * Unit tests for {@link LearningDatasetController}.
 */
public class LearningDatasetControllerTest {
    @Mock
    private LearningDatasetService learningDatasetService;

    @InjectMocks
    private LearningDatasetController learningDatasetController;

    private LearningDataset learningDataset1;
    private LearningDataset learningDataset2;

    /**
     * Sets up test data and initializes mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        learningDataset1 = new LearningDataset(1L, 1L, 1L, "Learning Dataset Description 1");
        learningDataset2 = new LearningDataset(2L, 1L, 1L, "Learning Dataset Description 2");
    }

    /**
     * Tests the {@link LearningDatasetController#getAllLearningDatasets()} method.
     * Verifies that all learningDatasets are returned with a status of 200 OK.
     */
    @Test
    void testGetAllLearningDatasets() {
        when(learningDatasetService.getAllLearningDatasets()).thenReturn(Arrays.asList(learningDataset1, learningDataset2));

        ResponseEntity<List<LearningDataset>> response = learningDatasetController.getAllLearningDatasets();

        HttpHeaders headers = response.getHeaders();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        assertEquals("2", headers.getFirst("X-Total-Count"));
        verify(learningDatasetService, times(1)).getAllLearningDatasets();
    }

    /**
     * Tests the {@link LearningDatasetController#getLearningDatasetsByTransformationId(Long)} method.
     * Verifies that all learningDatasets with given transformationId is returned with a status of 200 OK when found.
     */
    @Test
    void testGetLearningDatasetsByTransformationIdFound() {
        when(learningDatasetService.findByDataTransformationId(1L)).thenReturn(Arrays.asList(learningDataset1, learningDataset2));

        ResponseEntity<List<LearningDataset>> response = learningDatasetController.getLearningDatasetsByTransformationId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        verify(learningDatasetService, times(1)).findByDataTransformationId(1L);
    }

    /**
     * Tests the {@link LearningDatasetController#getLearningDatasetsByTransformationId(Long)} method.
     * Verifies that a status of 404 Not Found is returned when no learning dataset with given transformationId is found.
     */
    @Test
    void testGetLearningDatasetsByTransformationIdNotFound() {
        when(learningDatasetService.findByDataTransformationId(1L)).thenReturn(Collections.emptyList());

        ResponseEntity<List<LearningDataset>> response = learningDatasetController.getLearningDatasetsByTransformationId(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(learningDatasetService, times(1)).findByDataTransformationId(1L);
    }

    /**
     * Tests the {@link LearningDatasetController#getLearningDatasetsByDatasetId(Long)} method.
     * Verifies that all learningDatasets with given datasetId is returned with a status of 200 OK when found.
     */
    @Test
    void testGetLearningDatasetsByDatasetIdFound() {
        when(learningDatasetService.findByDatasetId(1L)).thenReturn(Arrays.asList(learningDataset1, learningDataset2));

        ResponseEntity<List<LearningDataset>> response = learningDatasetController.getLearningDatasetsByDatasetId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        verify(learningDatasetService, times(1)).findByDatasetId(1L);
    }

    /**
     * Tests the {@link LearningDatasetController#getLearningDatasetsByDatasetId(Long)} method.
     * Verifies that a status of 404 Not Found is returned when no learning dataset with given datasetId is found.
     */
    @Test
    void testGetLearningDatasetsByDatasetIdNotFound() {
        when(learningDatasetService.findByDatasetId(1L)).thenReturn(Collections.emptyList());

        ResponseEntity<List<LearningDataset>> response = learningDatasetController.getLearningDatasetsByDatasetId(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(learningDatasetService, times(1)).findByDatasetId(1L);
    }

    /**
     * Tests the {@link LearningDatasetController#getLearningDataset(Long)} method.
     * Verifies that a learningDataset with given learningDatasetId is returned with a status of 200 OK when found.
     */
    @Test
    void testGetLearningDatasetFound() {
        when(learningDatasetService.findLearningDatasetByLearningDatasetId(1L)).thenReturn(Optional.of(learningDataset1));

        ResponseEntity<?> response = learningDatasetController.getLearningDataset(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(learningDataset1, response.getBody());
        verify(learningDatasetService, times(1)).findLearningDatasetByLearningDatasetId(1L);
    }

    /**
     * Tests the {@link LearningDatasetController#getLearningDataset(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the learningDataset is not found.
     */
    @Test
    void testGetLearningDatasetNotFound() {
        when(learningDatasetService.findLearningDatasetByLearningDatasetId(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = learningDatasetController.getLearningDataset(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(learningDatasetService, times(1)).findLearningDatasetByLearningDatasetId(1L);
    }

    /**
     * Tests the {@link LearningDatasetController#createLearningDataset(LearningDataset)} method.
     * Verifies that a learningDataset is created successfully with a status of 201 Created.
     */
    @Test
    void testCreateLearningDatasetSuccess() {
        when(learningDatasetService.saveLearningDataset(learningDataset1)).thenReturn(learningDataset1);

        ResponseEntity<?> response = learningDatasetController.createLearningDataset(learningDataset1);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(learningDataset1, response.getBody());
        verify(learningDatasetService, times(1)).saveLearningDataset(learningDataset1);
    }

    /**
     * Tests the {@link LearningDatasetController#createLearningDataset(LearningDataset)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testCreateLearningDatasetFailure() {
        when(learningDatasetService.saveLearningDataset(learningDataset1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = learningDatasetController.createLearningDataset(learningDataset1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(learningDatasetService, times(1)).saveLearningDataset(learningDataset1);
    }

    /**
     * Tests the {@link LearningDatasetController#updateLearningDataset(Long, LearningDataset)} method.
     * Verifies that a learningDataset is updated successfully with a status of 200 OK when found.
     */
    @Test
    void testUpdateLearningDatasetFound() {
        when(learningDatasetService.updateLearningDataset(1L, learningDataset1)).thenReturn(Optional.of(learningDataset1));

        ResponseEntity<?> response = learningDatasetController.updateLearningDataset(1L, learningDataset1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Optional.of(learningDataset1), response.getBody());
        verify(learningDatasetService, times(1)).updateLearningDataset(1L, learningDataset1);
    }

    /**
     * Tests the {@link LearningDatasetController#updateLearningDataset(Long, LearningDataset)} method.
     * Verifies that a status of 404 Not Found is returned when the learningDataset to update is not found.
     */
    @Test
    void testUpdateLearningDatasetNotFound() {
        when(learningDatasetService.updateLearningDataset(1L, learningDataset1)).thenReturn(Optional.empty());

        ResponseEntity<?> response = learningDatasetController.updateLearningDataset(1L, learningDataset1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(learningDatasetService, times(1)).updateLearningDataset(1L, learningDataset1);
    }

    /**
     * Tests the {@link LearningDatasetController#updateLearningDataset(Long, LearningDataset)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testUpdateLearningDatasetFailure() {
        when(learningDatasetService.updateLearningDataset(1L, learningDataset1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = learningDatasetController.updateLearningDataset(1L, learningDataset1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(learningDatasetService, times(1)).updateLearningDataset(1L, learningDataset1);
    }

    /**
     * Tests the {@link LearningDatasetController#deleteLearningDataset(Long)} method.
     * Verifies that a learningDataset is deleted successfully with a status of 204 No Content when found.
     */
    @Test
    void testDeleteLearningDatasetFound() {
        when(learningDatasetService.deleteLearningDataset(1L)).thenReturn(true);

        ResponseEntity<?> response = learningDatasetController.deleteLearningDataset(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(learningDatasetService, times(1)).deleteLearningDataset(1L);
    }

    /**
     * Tests the {@link LearningDatasetController#deleteLearningDataset(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the learningDataset to delete is not found.
     */
    @Test
    void testDeleteLearningDatasetNotFound() {
        when(learningDatasetService.deleteLearningDataset(1L)).thenReturn(false);

        ResponseEntity<?> response = learningDatasetController.deleteLearningDataset(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(learningDatasetService, times(1)).deleteLearningDataset(1L);
    }

    /**
     * Tests the {@link LearningDatasetController#deleteLearningDataset(Long)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testDeleteLearningDatasetFailure() {
        when(learningDatasetService.deleteLearningDataset(1L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = learningDatasetController.deleteLearningDataset(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(learningDatasetService, times(1)).deleteLearningDataset(1L);
    }
}
