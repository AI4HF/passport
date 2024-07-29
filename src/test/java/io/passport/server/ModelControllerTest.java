package io.passport.server;

import io.passport.server.controller.ModelController;
import io.passport.server.model.Model;
import io.passport.server.service.ModelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
 * Unit tests for {@link ModelController}.
 */
public class ModelControllerTest {
    @Mock
    private ModelService modelService;

    @InjectMocks
    private ModelController modelController;

    private Model model1;
    private Model model2;

    /**
     * Sets up test data and initializes mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        model1 = new Model(1L, 1L, 1L, "Model Name 1", "1.0", "Tag1", "Type1", "Product1", 1L, "TRL1", "License1", "Primary Use 1", "Secondary Use 1", "Intended Users 1", "Counter Indications 1", "Ethical Considerations 1", "Limitations 1", "Fairness Constraints 1", Instant.now(), 1L, Instant.now(), 1L);
        model2 = new Model(2L, 1L, 1L, "Model Name 2", "2.0", "Tag2", "Type2", "Product2", 1L, "TRL2", "License2", "Primary Use 2", "Secondary Use 2", "Intended Users 2", "Counter Indications 2", "Ethical Considerations 2", "Limitations 2", "Fairness Constraints 2", Instant.now(), 1L, Instant.now(), 1L);
    }

    /**
     * Tests the {@link ModelController#getAllModels(Long)} method.
     * Verifies that all models are returned with a status of 200 OK.
     */
    @Test
    void testGetAllModelsNoParam() {
        when(modelService.getAllModels()).thenReturn(Arrays.asList(model1, model2));

        ResponseEntity<List<Model>> response = modelController.getAllModels(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        verify(modelService, times(1)).getAllModels();
    }

    /**
     * Tests the {@link ModelController#getAllModels(Long)}()} method.
     * Verifies that all models with the given studyId are returned with a status of 200 OK.
     */
    @Test
    void testGetAllModelsWithParam() {
        when(modelService.getAllModelsByStudyId(1L)).thenReturn(Arrays.asList(model1, model2));

        ResponseEntity<List<Model>> response = modelController.getAllModels(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        verify(modelService, times(1)).getAllModelsByStudyId(1L);
    }

    /**
     * Tests the {@link ModelController#getModelById(Long)} method.
     * Verifies that a model is returned with a status of 200 OK when found.
     */
    @Test
    void testGetModelByIdFound() {
        when(modelService.findModelById(1L)).thenReturn(Optional.of(model1));

        ResponseEntity<?> response = modelController.getModelById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Optional.of(model1), response.getBody());
        verify(modelService, times(1)).findModelById(1L);
    }

    /**
     * Tests the {@link ModelController#getModelById(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the model is not found.
     */
    @Test
    void testGetModelByIdNotFound() {
        when(modelService.findModelById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = modelController.getModelById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(modelService, times(1)).findModelById(1L);
    }

    /**
     * Tests the {@link ModelController#createModel(Model)} method.
     * Verifies that a model is created successfully with a status of 201 Created.
     */
    @Test
    void testCreateModelSuccess() {
        when(modelService.saveModel(model1)).thenReturn(model1);

        ResponseEntity<?> response = modelController.createModel(model1);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(model1, response.getBody());
        verify(modelService, times(1)).saveModel(model1);
    }

    /**
     * Tests the {@link ModelController#createModel(Model)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testCreateModelFailure() {
        when(modelService.saveModel(model1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = modelController.createModel(model1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(modelService, times(1)).saveModel(model1);
    }

    /**
     * Tests the {@link ModelController#updateModel(Long, Model)} method.
     * Verifies that a model is updated successfully with a status of 200 OK when found.
     */
    @Test
    void testUpdateModelFound() {
        when(modelService.updateModel(1L, model1)).thenReturn(Optional.of(model1));

        ResponseEntity<?> response = modelController.updateModel(1L, model1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(model1, response.getBody());
        verify(modelService, times(1)).updateModel(1L, model1);
    }

    /**
     * Tests the {@link ModelController#updateModel(Long, Model)} method.
     * Verifies that a status of 404 Not Found is returned when the model to update is not found.
     */
    @Test
    void testUpdateModelNotFound() {
        when(modelService.updateModel(1L, model1)).thenReturn(Optional.empty());

        ResponseEntity<?> response = modelController.updateModel(1L, model1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(modelService, times(1)).updateModel(1L, model1);
    }

    /**
     * Tests the {@link ModelController#updateModel(Long, Model)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testUpdateModelFailure() {
        when(modelService.updateModel(1L, model1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = modelController.updateModel(1L, model1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(modelService, times(1)).updateModel(1L, model1);
    }

    /**
     * Tests the {@link ModelController#deleteModel(Long)} method.
     * Verifies that a model is deleted successfully with a status of 204 No Content when found.
     */
    @Test
    void testDeleteModelFound() {
        when(modelService.deleteModel(1L)).thenReturn(true);

        ResponseEntity<?> response = modelController.deleteModel(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(modelService, times(1)).deleteModel(1L);
    }

    /**
     * Tests the {@link ModelController#deleteModel(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the model to delete is not found.
     */
    @Test
    void testDeleteModelNotFound() {
        when(modelService.deleteModel(1L)).thenReturn(false);

        ResponseEntity<?> response = modelController.deleteModel(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(modelService, times(1)).deleteModel(1L);
    }

    /**
     * Tests the {@link ModelController#deleteModel(Long)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testDeleteModelFailure() {
        when(modelService.deleteModel(1L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = modelController.deleteModel(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(modelService, times(1)).deleteModel(1L);
    }
}
