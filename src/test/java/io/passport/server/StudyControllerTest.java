package io.passport.server;

import io.passport.server.controller.StudyController;
import io.passport.server.model.Study;
import io.passport.server.service.StudyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StudyController}.
 */
class StudyControllerTest {

    @Mock
    private StudyService studyService;

    @InjectMocks
    private StudyController studyController;

    private Study study1;
    private Study study2;

    /**
     * Sets up test data and initializes mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        study1 = new Study(1L, "Study 1", "Description 1", "Objectives 1", "Ethics 1", "1");
        study2 = new Study(2L, "Study 2", "Description 2", "Objectives 2", "Ethics 2", "1");
    }

    /**
     * Tests the {@link StudyController#getAllStudies()} method.
     * Verifies that all studies are returned with a status of 200 OK.
     */
    @Test
    void testGetAllStudies() {
        when(studyService.getAllStudies()).thenReturn(Arrays.asList(study1, study2));

        ResponseEntity<List<Study>> response = studyController.getAllStudies();

        HttpHeaders headers = response.getHeaders();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        assertEquals("2", headers.getFirst("X-Total-Count"));
        verify(studyService, times(1)).getAllStudies();
    }

    /**
     * Tests the {@link StudyController#getStudy(Long)} method.
     * Verifies that a study is returned with a status of 200 OK when found.
     */
    @Test
    void testGetStudyFound() {
        when(studyService.findStudyByStudyId(1L)).thenReturn(Optional.of(study1));

        ResponseEntity<?> response = studyController.getStudy(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(study1, response.getBody());
        verify(studyService, times(1)).findStudyByStudyId(1L);
    }

    /**
     * Tests the {@link StudyController#getStudy(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the study is not found.
     */
    @Test
    void testGetStudyNotFound() {
        when(studyService.findStudyByStudyId(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = studyController.getStudy(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(studyService, times(1)).findStudyByStudyId(1L);
    }

    /**
     * Tests the {@link StudyController#createStudy(Study)} method.
     * Verifies that a study is created successfully with a status of 201 Created.
     */
    @Test
    void testCreateStudySuccess() {
        when(studyService.saveStudy(study1)).thenReturn(study1);

        ResponseEntity<?> response = studyController.createStudy(study1);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(study1, response.getBody());
        verify(studyService, times(1)).saveStudy(study1);
    }

    /**
     * Tests the {@link StudyController#createStudy(Study)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testCreateStudyFailure() {
        when(studyService.saveStudy(study1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = studyController.createStudy(study1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studyService, times(1)).saveStudy(study1);
    }

    /**
     * Tests the {@link StudyController#updateStudy(Long, Study)} method.
     * Verifies that a study is updated successfully with a status of 200 OK when found.
     */
    @Test
    void testUpdateStudyFound() {
        when(studyService.updateStudy(1L, study1)).thenReturn(Optional.of(study1));

        ResponseEntity<?> response = studyController.updateStudy(1L, study1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Optional.of(study1), response.getBody());
        verify(studyService, times(1)).updateStudy(1L, study1);
    }

    /**
     * Tests the {@link StudyController#updateStudy(Long, Study)} method.
     * Verifies that a status of 404 Not Found is returned when the study to update is not found.
     */
    @Test
    void testUpdateStudyNotFound() {
        when(studyService.updateStudy(1L, study1)).thenReturn(Optional.empty());

        ResponseEntity<?> response = studyController.updateStudy(1L, study1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(studyService, times(1)).updateStudy(1L, study1);
    }

    /**
     * Tests the {@link StudyController#updateStudy(Long, Study)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testUpdateStudyFailure() {
        when(studyService.updateStudy(1L, study1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = studyController.updateStudy(1L, study1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studyService, times(1)).updateStudy(1L, study1);
    }

    /**
     * Tests the {@link StudyController#deleteStudy(Long)} method.
     * Verifies that a study is deleted successfully with a status of 204 No Content when found.
     */
    @Test
    void testDeleteStudyFound() {
        when(studyService.deleteStudy(1L)).thenReturn(true);

        ResponseEntity<?> response = studyController.deleteStudy(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(studyService, times(1)).deleteStudy(1L);
    }

    /**
     * Tests the {@link StudyController#deleteStudy(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the study to delete is not found.
     */
    @Test
    void testDeleteStudyNotFound() {
        when(studyService.deleteStudy(1L)).thenReturn(false);

        ResponseEntity<?> response = studyController.deleteStudy(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(studyService, times(1)).deleteStudy(1L);
    }

    /**
     * Tests the {@link StudyController#deleteStudy(Long)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testDeleteStudyFailure() {
        when(studyService.deleteStudy(1L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = studyController.deleteStudy(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studyService, times(1)).deleteStudy(1L);
    }
}


