package io.passport.server;

import io.passport.server.controller.SurveyController;
import io.passport.server.model.Survey;
import io.passport.server.service.SurveyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SurveyController}.
 */
class SurveyControllerTest {

    @Mock
    private SurveyService surveyService;

    @InjectMocks
    private SurveyController surveyController;

    private Survey survey1;
    private Survey survey2;

    /**
     * Sets up test data and initializes mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        survey1 = new Survey(1L, 1L, "Question 1", "Answer 1", "Category 1");
        survey2 = new Survey(2L, 1L, "Question 2", "Answer 2", "Category 2");
    }

    /**
     * Tests the {@link SurveyController#getAllSurveys()} method.
     * Verifies that all surveys are returned with a status of 200 OK.
     */
    @Test
    void testGetAllSurveys() {
        when(surveyService.findAllSurveys()).thenReturn(Arrays.asList(survey1, survey2));

        ResponseEntity<List<Survey>> response = surveyController.getAllSurveys();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        verify(surveyService, times(1)).findAllSurveys();
    }

    /**
     * Tests the {@link SurveyController#getSurveyById(Long)} method.
     * Verifies that a survey is returned with a status of 200 OK when found.
     */
    @Test
    void testGetSurveyByIdFound() {
        when(surveyService.findSurveyById(1L)).thenReturn(Optional.of(survey1));

        ResponseEntity<?> response = surveyController.getSurveyById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(survey1, response.getBody());
        verify(surveyService, times(1)).findSurveyById(1L);
    }

    /**
     * Tests the {@link SurveyController#getSurveyById(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the survey is not found.
     */
    @Test
    void testGetSurveyByIdNotFound() {
        when(surveyService.findSurveyById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = surveyController.getSurveyById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(surveyService, times(1)).findSurveyById(1L);
    }

    /**
     * Tests the {@link SurveyController#getSurveysByStudyId(Long)} method.
     * Verifies that surveys are returned with a status of 200 OK.
     */
    @Test
    void testGetSurveysByStudyId() {
        when(surveyService.findSurveysByStudyId(1L)).thenReturn(Arrays.asList(survey1, survey2));

        ResponseEntity<List<Survey>> response = surveyController.getSurveysByStudyId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        verify(surveyService, times(1)).findSurveysByStudyId(1L);
    }

    /**
     * Tests the {@link SurveyController#createSurvey(Survey)} method.
     * Verifies that a survey is created successfully with a status of 201 Created.
     */
    @Test
    void testCreateSurveySuccess() {
        when(surveyService.saveSurvey(survey1)).thenReturn(survey1);

        ResponseEntity<?> response = surveyController.createSurvey(survey1);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(survey1, response.getBody());
        verify(surveyService, times(1)).saveSurvey(survey1);
    }

    /**
     * Tests the {@link SurveyController#createSurvey(Survey)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testCreateSurveyFailure() {
        when(surveyService.saveSurvey(survey1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = surveyController.createSurvey(survey1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(surveyService, times(1)).saveSurvey(survey1);
    }

    /**
     * Tests the {@link SurveyController#updateSurvey(Long, Survey)} method.
     * Verifies that a survey is updated successfully with a status of 200 OK when found.
     */
    @Test
    void testUpdateSurveyFound() {
        when(surveyService.updateSurvey(1L, survey1)).thenReturn(Optional.of(survey1));

        ResponseEntity<?> response = surveyController.updateSurvey(1L, survey1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(survey1, response.getBody());
        verify(surveyService, times(1)).updateSurvey(1L, survey1);
    }

    /**
     * Tests the {@link SurveyController#updateSurvey(Long, Survey)} method.
     * Verifies that a status of 404 Not Found is returned when the survey to update is not found.
     */
    @Test
    void testUpdateSurveyNotFound() {
        when(surveyService.updateSurvey(1L, survey1)).thenReturn(Optional.empty());

        ResponseEntity<?> response = surveyController.updateSurvey(1L, survey1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(surveyService, times(1)).updateSurvey(1L, survey1);
    }

    /**
     * Tests the {@link SurveyController#updateSurvey(Long, Survey)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testUpdateSurveyFailure() {
        when(surveyService.updateSurvey(1L, survey1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = surveyController.updateSurvey(1L, survey1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(surveyService, times(1)).updateSurvey(1L, survey1);
    }

    /**
     * Tests the {@link SurveyController#deleteSurvey(Long)} method.
     * Verifies that a survey is deleted successfully with a status of 204 No Content when found.
     */
    @Test
    void testDeleteSurveyFound() {
        when(surveyService.deleteSurvey(1L)).thenReturn(true);

        ResponseEntity<?> response = surveyController.deleteSurvey(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(surveyService, times(1)).deleteSurvey(1L);
    }

    /**
     * Tests the {@link SurveyController#deleteSurvey(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the survey to delete is not found.
     */
    @Test
    void testDeleteSurveyNotFound() {
        when(surveyService.deleteSurvey(1L)).thenReturn(false);

        ResponseEntity<?> response = surveyController.deleteSurvey(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(surveyService, times(1)).deleteSurvey(1L);
    }

    /**
     * Tests the {@link SurveyController#deleteSurvey(Long)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testDeleteSurveyFailure() {
        when(surveyService.deleteSurvey(1L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = surveyController.deleteSurvey(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(surveyService, times(1)).deleteSurvey(1L);
    }
}
