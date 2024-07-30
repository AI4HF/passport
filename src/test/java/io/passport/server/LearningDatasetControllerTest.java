package io.passport.server;

import io.passport.server.controller.LearningDatasetController;
import io.passport.server.model.DatasetTransformation;
import io.passport.server.model.LearningDataset;
import io.passport.server.model.LearningDatasetandTransformationDTO;
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
    private DatasetTransformation datasetTransformation;
    private LearningDatasetandTransformationDTO learningDatasetandTransformationDTO;

    /**
     * Sets up test data and initializes mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        learningDataset1 = new LearningDataset(1L, 1L, 1L, "Learning Dataset Description 1");
        learningDataset2 = new LearningDataset(2L, 1L, 1L, "Learning Dataset Description 2");
        datasetTransformation = new DatasetTransformation(1L, "Title", "Description");
        learningDatasetandTransformationDTO = new LearningDatasetandTransformationDTO(datasetTransformation, learningDataset1);
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
     * Tests the {@link LearningDatasetController#getLearningDatasets(Long, Long)} method.
     * Verifies that all learningDatasets are returned with a status of 200 OK.
     */
    @Test
    void testGetLearningDatasetsNoParam() {
        when(learningDatasetService.getAllLearningDatasets()).thenReturn(Arrays.asList(learningDataset1, learningDataset2));

        ResponseEntity<List<LearningDataset>> response = learningDatasetController.getLearningDatasets(null, null);

        HttpHeaders headers = response.getHeaders();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        verify(learningDatasetService, times(1)).getAllLearningDatasets();
    }

    /**
     * Tests the {@link LearningDatasetController#getLearningDatasets(Long, Long)} method.
     * Verifies that all learningDatasets with given transformationId is returned with a status of 200 OK.
     */
    @Test
    void testGetLearningDatasetsWithDataTransformationId() {
        when(learningDatasetService.findByDataTransformationId(1L)).thenReturn(Arrays.asList(learningDataset1, learningDataset2));

        ResponseEntity<List<LearningDataset>> response = learningDatasetController.getLearningDatasets(1L, null);

        HttpHeaders headers = response.getHeaders();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        verify(learningDatasetService, times(1)).findByDataTransformationId(1L);
    }

    /**
     * Tests the {@link LearningDatasetController#getLearningDatasets(Long, Long)} method.
     * Verifies that all learningDatasets with given datasetId is returned with a status of 200 OK.
     */
    @Test
    void testGetLearningDatasetsWithDatasetId() {
        when(learningDatasetService.findByDatasetId(1L)).thenReturn(Arrays.asList(learningDataset1, learningDataset2));

        ResponseEntity<List<LearningDataset>> response = learningDatasetController.getLearningDatasets(null, 1L);

        HttpHeaders headers = response.getHeaders();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        verify(learningDatasetService, times(1)).findByDatasetId(1L);
    }

    /**
     * Tests the {@link LearningDatasetController#getLearningDatasets(Long, Long)} method.
     * Verifies that a bad request status is returned when both dataTransformationId and datasetId are provided.
     */
    @Test
    void testGetAllLearningDatasetsWithDataTransformationIdAndDatasetId() {
        ResponseEntity<List<LearningDataset>> response = learningDatasetController.getLearningDatasets(1L, 1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(learningDatasetService, times(0)).findByDataTransformationId(anyLong());
        verify(learningDatasetService, times(0)).findByDatasetId(anyLong());
        verify(learningDatasetService, times(0)).getAllLearningDatasets();
    }

    /**
     * Tests the {@link LearningDatasetController#createLearningDatasetWithTransformation(LearningDatasetandTransformationDTO)} method.
     * Verifies that a learningDataset is created successfully with a status of 201 Created.
     */
    @Test
    void testCreateLearningDatasetWithTransformationSuccess() {
        when(learningDatasetService.createLearningDatasetAndTransformation(learningDatasetandTransformationDTO)).thenReturn(learningDatasetandTransformationDTO);

        ResponseEntity<?> result = learningDatasetController.createLearningDatasetWithTransformation(learningDatasetandTransformationDTO);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(learningDatasetandTransformationDTO, result.getBody());
        verify(learningDatasetService, times(1)).createLearningDatasetAndTransformation(learningDatasetandTransformationDTO);
    }

    /**
     * Tests the {@link LearningDatasetController#createLearningDatasetWithTransformation(LearningDatasetandTransformationDTO)}  method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testCreateLearningDatasetWithTransformationFailure() {
        when(learningDatasetService.createLearningDatasetAndTransformation(learningDatasetandTransformationDTO)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = learningDatasetController.createLearningDatasetWithTransformation(learningDatasetandTransformationDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(learningDatasetService, times(1)).createLearningDatasetAndTransformation(learningDatasetandTransformationDTO);
    }

    /**
     * Tests the {@link LearningDatasetController#updateLearningDatasetWithTransformation(Long, LearningDatasetandTransformationDTO)}  method.
     * Verifies that a learningDataset is updated successfully with a status of 200 OK when found.
     */
    @Test
    void testUpdateLearningDatasetWithTransformationFound() {
        when(learningDatasetService.updateLearningDatasetWithTransformation(datasetTransformation, learningDataset1)).thenReturn(Optional.of(learningDatasetandTransformationDTO));

        ResponseEntity<?> response = learningDatasetController.updateLearningDatasetWithTransformation(1L, learningDatasetandTransformationDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(learningDatasetandTransformationDTO, response.getBody());
        verify(learningDatasetService, times(1)).updateLearningDatasetWithTransformation(datasetTransformation, learningDataset1);
    }

    /**
     * Tests the {@link LearningDatasetController#updateLearningDatasetWithTransformation(Long, LearningDatasetandTransformationDTO)}  method.
     * Verifies that a status of 404 Not Found is returned when the learningDataset to update is not found.
     */
    @Test
    void testUpdateLearningDatasetWithTransformationNotFound() {
        when(learningDatasetService.updateLearningDatasetWithTransformation(datasetTransformation, learningDataset1)).thenReturn(Optional.empty());

        ResponseEntity<?> response = learningDatasetController.updateLearningDatasetWithTransformation(1L, learningDatasetandTransformationDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(learningDatasetService, times(1)).updateLearningDatasetWithTransformation(datasetTransformation, learningDataset1);
    }

    /**
     * Tests the {@link LearningDatasetController#updateLearningDatasetWithTransformation(Long, LearningDatasetandTransformationDTO)}  method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testUpdateLearningDatasetWithTransformationFailure() {
        when(learningDatasetService.updateLearningDatasetWithTransformation(datasetTransformation, learningDataset1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = learningDatasetController.updateLearningDatasetWithTransformation(1L, learningDatasetandTransformationDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(learningDatasetService, times(1)).updateLearningDatasetWithTransformation(datasetTransformation, learningDataset1);
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
