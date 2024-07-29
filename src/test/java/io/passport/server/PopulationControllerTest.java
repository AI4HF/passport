package io.passport.server;

import io.passport.server.controller.PopulationController;
import io.passport.server.model.Population;
import io.passport.server.service.PopulationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PopulationController}.
 */
class PopulationControllerTest {

    @Mock
    private PopulationService populationService;

    @InjectMocks
    private PopulationController populationController;

    private Population population1;
    private Population population2;

    /**
     * Sets up test data and initializes mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        population1 = new Population(1L, 1L, "https://example.com/population1", "Description 1", "Characteristics 1");
        population2 = new Population(2L, 1L, "https://example.com/population2", "Description 2", "Characteristics 2");
    }

    /**
     * Tests the {@link PopulationController#getPopulationById(Long)} method.
     * Verifies that population is returned with a status of 200 OK when found.
     */
    @Test
    void testGetPopulationByIdFound() {
        when(populationService.findPopulationById(1L)).thenReturn(Optional.of(population1));

        ResponseEntity<?> response = populationController.getPopulationById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Optional.of(population1), response.getBody());
        verify(populationService, times(1)).findPopulationById(1L);
    }

    /**
     * Tests the {@link PopulationController#getPopulationById(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the population is not found.
     */
    @Test
    void testGetPopulationByIdNotFound() {
        when(populationService.findPopulationById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = populationController.getPopulationById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(populationService, times(1)).findPopulationById(1L);
    }

    /**
     * Tests the {@link PopulationController#getPopulationByStudyId(Long)} method.
     * Verifies that all populations are returned with a status of 200 OK when studyId param is not given.
     */
    @Test
    void testGetPopulationByStudyIdNoParam() {
        when(populationService.findAllPopulations()).thenReturn(Arrays.asList(population1, population2));

        ResponseEntity<?> response = populationController.getPopulationByStudyId(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List<?>);
        List<?> responseBody = (List<?>) response.getBody();
        assertEquals(2, responseBody.size());
        verify(populationService, times(1)).findAllPopulations();
    }

    /**
     * Tests the {@link PopulationController#getPopulationByStudyId(Long)} method.
     * Verifies that all populations with the given studyId is returned with a status of 200 OK when found.
     */
    @Test
    void testGetPopulationByStudyIdWithParam() {
        when(populationService.findPopulationByStudyId(1L)).thenReturn(Arrays.asList(population1, population2));

        ResponseEntity<?> response = populationController.getPopulationByStudyId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List<?>);
        List<?> responseBody = (List<?>) response.getBody();
        assertEquals(2, responseBody.size());
        verify(populationService, times(1)).findPopulationByStudyId(1L);
    }

    /**
     * Tests the {@link PopulationController#createPopulation(Population)} method.
     * Verifies that population is created successfully with a status of 201 Created.
     */
    @Test
    void testCreatePopulationSuccess() {
        when(populationService.savePopulation(population1)).thenReturn(population1);

        ResponseEntity<?> response = populationController.createPopulation(population1);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(population1, response.getBody());
        verify(populationService, times(1)).savePopulation(population1);
    }

    /**
     * Tests the {@link PopulationController#createPopulation(Population)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testCreatePopulationFailure() {
        when(populationService.savePopulation(population1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = populationController.createPopulation(population1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(populationService, times(1)).savePopulation(population1);
    }

    /**
     * Tests the {@link PopulationController#updatePopulation(Long, Population)} method.
     * Verifies that population is updated successfully with a status of 200 OK when found.
     */
    @Test
    void testUpdatePopulationFound() {
        when(populationService.updatePopulation(1L, population1)).thenReturn(Optional.of(population1));

        ResponseEntity<?> response = populationController.updatePopulation(1L, population1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(population1, response.getBody());
        verify(populationService, times(1)).updatePopulation(1L, population1);
    }

    /**
     * Tests the {@link PopulationController#updatePopulation(Long, Population)} method.
     * Verifies that a status of 404 Not Found is returned when the population to update is not found.
     */
    @Test
    void testUpdatePopulationNotFound() {
        when(populationService.updatePopulation(1L, population1)).thenReturn(Optional.empty());

        ResponseEntity<?> response = populationController.updatePopulation(1L, population1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(populationService, times(1)).updatePopulation(1L, population1);
    }

    /**
     * Tests the {@link PopulationController#updatePopulation(Long, Population)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testUpdatePopulationFailure() {
        when(populationService.updatePopulation(1L, population1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = populationController.updatePopulation(1L, population1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(populationService, times(1)).updatePopulation(1L, population1);
    }

    /**
     * Tests the {@link PopulationController#deletePopulation(Long)} method.
     * Verifies that population is deleted successfully with a status of 204 No Content when found.
     */
    @Test
    void testDeletePopulationFound() {
        when(populationService.deletePopulation(1L)).thenReturn(true);

        ResponseEntity<?> response = populationController.deletePopulation(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(populationService, times(1)).deletePopulation(1L);
    }

    /**
     * Tests the {@link PopulationController#deletePopulation(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the population to delete is not found.
     */
    @Test
    void testDeletePopulationNotFound() {
        when(populationService.deletePopulation(1L)).thenReturn(false);

        ResponseEntity<?> response = populationController.deletePopulation(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(populationService, times(1)).deletePopulation(1L);
    }

    /**
     * Tests the {@link PopulationController#deletePopulation(Long)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testDeletePopulationFailure() {
        when(populationService.deletePopulation(1L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = populationController.deletePopulation(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(populationService, times(1)).deletePopulation(1L);
    }
}
