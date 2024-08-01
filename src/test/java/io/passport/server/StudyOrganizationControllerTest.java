package io.passport.server;

import io.passport.server.controller.StudyOrganizationController;
import io.passport.server.model.*;
import io.passport.server.service.StudyOrganizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static io.passport.server.model.Role.STUDY_OWNER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StudyOrganizationController}.
 */
class StudyOrganizationControllerTest {

    @Mock
    private StudyOrganizationService studyOrganizationService;

    @InjectMocks
    private StudyOrganizationController studyOrganizationController;

    private StudyOrganizationDTO studyOrganizationDTO1;
    private StudyOrganization studyOrganization1;

    /**
     * Sets up test data and initializes mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        studyOrganizationDTO1 = new StudyOrganizationDTO(1L, 1L, "1", new HashSet<>(List.of(STUDY_OWNER)), 1L);
        studyOrganization1 = new StudyOrganization(studyOrganizationDTO1);
    }

    /**
     * Tests the {@link StudyOrganizationController#getStudyOrganizationByStudyOrganizationId(Long, Long)} method.
     * Verifies that a study organization is returned with a status of 200 OK when found.
     */
    @Test
    void testGetStudyOrganizationByStudyOrganizationIdFound() {
        when(studyOrganizationService.findStudyOrganizationById(argThat(argument ->
                argument.getOrganizationId().equals(1L) && argument.getStudyId().equals(1L))))
                .thenReturn(Optional.of(studyOrganization1));

        ResponseEntity<?> response = studyOrganizationController.getStudyOrganizationByStudyOrganizationId(1L, 1L);

        StudyOrganizationDTO responseBody = (StudyOrganizationDTO) response.getBody();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(studyOrganization1.getId().getOrganizationId(), responseBody.getOrganizationId());
        assertEquals(studyOrganization1.getId().getStudyId(), responseBody.getStudyId());
        verify(studyOrganizationService, times(1)).findStudyOrganizationById(argThat(argument ->
                argument.getOrganizationId().equals(1L) && argument.getStudyId().equals(1L)));
    }

    /**
     * Tests the {@link StudyOrganizationController#getStudyOrganizationByStudyOrganizationId(Long, Long)} method.
     * Verifies that a status of 404 Not Found is returned when the study organization is not found.
     */
    @Test
    void testGetStudyOrganizationByStudyOrganizationIdNotFound() {
        when(studyOrganizationService.findStudyOrganizationById(argThat(argument ->
                argument.getOrganizationId().equals(1L) && argument.getStudyId().equals(1L))))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response = studyOrganizationController.getStudyOrganizationByStudyOrganizationId(1L, 1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(studyOrganizationService, times(1)).findStudyOrganizationById(argThat(argument ->
                argument.getOrganizationId().equals(1L) && argument.getStudyId().equals(1L)));
    }

    /**
     * Tests the {@link StudyOrganizationController#getStudyOrganizationByStudyOrganizationId(Long, Long)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testGetStudyOrganizationByStudyOrganizationIdFailure() {
        when(studyOrganizationService.findStudyOrganizationById(argThat(argument ->
                argument.getOrganizationId().equals(1L) && argument.getStudyId().equals(1L))))
                .thenThrow(new RuntimeException("error"));

        ResponseEntity<?> response = studyOrganizationController.getStudyOrganizationByStudyOrganizationId(1L, 1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("error", response.getBody());
        verify(studyOrganizationService, times(1)).findStudyOrganizationById(argThat(argument ->
                argument.getOrganizationId().equals(1L) && argument.getStudyId().equals(1L)));
    }

    /**
     * Tests the {@link StudyOrganizationController#getOrganizationsByStudyId(Long)} method.
     * Verifies that a list of organizations is returned with a status of 200 OK when found.
     */
    @Test
    void testGetOrganizationsByStudyIdSuccess() {
        when(studyOrganizationService.findOrganizationsByStudyId(1L)).thenReturn(Arrays.asList(new Organization(), new Organization()));

        ResponseEntity<?> response = studyOrganizationController.getOrganizationsByStudyId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List<?>);
        List<?> responseBody = (List<?>) response.getBody();
        assertEquals(2, responseBody.size());
        verify(studyOrganizationService, times(1)).findOrganizationsByStudyId(1L);
    }

    /**
     * Tests the {@link StudyOrganizationController#getOrganizationsByStudyId(Long)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testGetOrganizationsByStudyIdFailure() {
        when(studyOrganizationService.findOrganizationsByStudyId(1L)).thenThrow(new RuntimeException("error"));

        ResponseEntity<?> response = studyOrganizationController.getOrganizationsByStudyId(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("error", response.getBody());

        verify(studyOrganizationService, times(1)).findOrganizationsByStudyId(1L);
    }

    /**
     * Tests the {@link StudyOrganizationController#getStudiesByOrganizationId(Long)} method.
     * Verifies that a list of studies is returned with a status of 200 OK when found.
     */
    @Test
    void testGetStudiesByOrganizationIdSuccess() {
        when(studyOrganizationService.findStudiesByOrganizationId(1L)).thenReturn(Arrays.asList(new Study(), new Study()));

        ResponseEntity<?> response = studyOrganizationController.getStudiesByOrganizationId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List<?>);
        List<?> responseBody = (List<?>) response.getBody();
        assertEquals(2, responseBody.size());
        verify(studyOrganizationService, times(1)).findStudiesByOrganizationId(1L);
    }

    /**
     * Tests the {@link StudyOrganizationController#getStudiesByOrganizationId(Long)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testGetStudiesByOrganizationIdFailure() {
        when(studyOrganizationService.findStudiesByOrganizationId(1L)).thenThrow(new RuntimeException("error"));

        ResponseEntity<?> response = studyOrganizationController.getStudiesByOrganizationId(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("error", response.getBody());
        verify(studyOrganizationService, times(1)).findStudiesByOrganizationId(1L);
    }

    /**
     * Tests the {@link StudyOrganizationController#createStudyOrganization(StudyOrganizationDTO)} method.
     * Verifies that a studyOrganization is created successfully with a status of 201 Created.
     */
    @Test
    void testCreateStudyOrganizationSuccess() {
        when(studyOrganizationService.createStudyOrganizationEntries(argThat(argument ->
                argument.getId().getOrganizationId().equals(1L) && argument.getId().getStudyId().equals(1L))))
                .thenReturn(studyOrganization1);

        ResponseEntity<?> response = studyOrganizationController.createStudyOrganization(studyOrganizationDTO1);
        StudyOrganizationDTO responseBody = (StudyOrganizationDTO) response.getBody();

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(studyOrganizationDTO1.getOrganizationId(), responseBody.getOrganizationId());
        assertEquals(studyOrganizationDTO1.getStudyId(), responseBody.getStudyId());
        verify(studyOrganizationService, times(1)).createStudyOrganizationEntries(argThat(argument ->
                argument.getId().getOrganizationId().equals(1L) && argument.getId().getStudyId().equals(1L)));
    }

    /**
     * Tests the {@link StudyOrganizationController#createStudyOrganization(StudyOrganizationDTO)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testCreateStudyOrganizationFailure() {
        when(studyOrganizationService.createStudyOrganizationEntries(argThat(argument ->
                argument.getId().getOrganizationId().equals(1L) && argument.getId().getStudyId().equals(1L))))
                .thenThrow(new RuntimeException("error"));

        ResponseEntity<?> response = studyOrganizationController.createStudyOrganization(studyOrganizationDTO1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("error", response.getBody());
        verify(studyOrganizationService, times(1)).createStudyOrganizationEntries(argThat(argument ->
                argument.getId().getOrganizationId().equals(1L) && argument.getId().getStudyId().equals(1L)));
    }

    /**
     * Tests the {@link StudyOrganizationController#updateStudyOrganization(Long, Long, StudyOrganizationDTO)} method.
     * Verifies that a study organization is updated successfully with a status of 200 OK.
     */
    @Test
    void testUpdateStudyOrganizationSuccess() {
        when(studyOrganizationService.updateStudyOrganization(argThat(argument ->
                argument.getOrganizationId().equals(1L) && argument.getStudyId().equals(1L)), any(StudyOrganization.class)))
                .thenReturn(Optional.of(studyOrganization1));

        ResponseEntity<?> response = studyOrganizationController.updateStudyOrganization(1L, 1L, studyOrganizationDTO1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        StudyOrganizationDTO responseBody = (StudyOrganizationDTO) response.getBody();
        assertEquals(studyOrganizationDTO1.getOrganizationId(), responseBody.getOrganizationId());
        assertEquals(studyOrganizationDTO1.getStudyId(), responseBody.getStudyId());
        verify(studyOrganizationService, times(1)).updateStudyOrganization(argThat(argument ->
                argument.getOrganizationId().equals(1L) && argument.getStudyId().equals(1L)), any(StudyOrganization.class));
    }

    /**
     * Tests the {@link StudyOrganizationController#updateStudyOrganization(Long, Long, StudyOrganizationDTO)} method.
     * Verifies that a status of 404 Not Found is returned when the study organization is not found.
     */
    @Test
    void testUpdateStudyOrganizationNotFound() {
        when(studyOrganizationService.updateStudyOrganization(argThat(argument ->
                argument.getOrganizationId().equals(1L) && argument.getStudyId().equals(1L)), any(StudyOrganization.class)))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response = studyOrganizationController.updateStudyOrganization(1L, 1L, studyOrganizationDTO1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(studyOrganizationService, times(1)).updateStudyOrganization(argThat(argument ->
                argument.getOrganizationId().equals(1L) && argument.getStudyId().equals(1L)), any(StudyOrganization.class));
    }

    /**
     * Tests the {@link StudyOrganizationController#updateStudyOrganization(Long, Long, StudyOrganizationDTO)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testUpdateStudyOrganizationFailure() {
        when(studyOrganizationService.updateStudyOrganization(argThat(argument ->
                argument.getOrganizationId().equals(1L) && argument.getStudyId().equals(1L)), any(StudyOrganization.class)))
                .thenThrow(new RuntimeException("error"));

        ResponseEntity<?> response = studyOrganizationController.updateStudyOrganization(1L, 1L, studyOrganizationDTO1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("error", response.getBody());
        verify(studyOrganizationService, times(1)).updateStudyOrganization(argThat(argument ->
                argument.getOrganizationId().equals(1L) && argument.getStudyId().equals(1L)), any(StudyOrganization.class));
    }

    /**
     * Tests the {@link StudyOrganizationController#deleteStudyOrganization(Long, Long)} method.
     * Verifies that a study organization is deleted successfully with a status of 204 No Content.
     */
    @Test
    void testDeleteStudyOrganizationSuccess() {
        when(studyOrganizationService.deleteStudyOrganization(argThat(argument ->
                argument.getOrganizationId().equals(1L) && argument.getStudyId().equals(1L))))
                .thenReturn(true);

        ResponseEntity<?> response = studyOrganizationController.deleteStudyOrganization(1L, 1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(studyOrganizationService, times(1)).deleteStudyOrganization(argThat(argument ->
                argument.getOrganizationId().equals(1L) && argument.getStudyId().equals(1L)));
    }

    /**
     * Tests the {@link StudyOrganizationController#deleteStudyOrganization(Long, Long)} method.
     * Verifies that a status of 404 Not Found is returned when the study organization is not found.
     */
    @Test
    void testDeleteStudyOrganizationNotFound() {
        when(studyOrganizationService.deleteStudyOrganization(argThat(argument ->
                argument.getOrganizationId().equals(1L) && argument.getStudyId().equals(1L))))
                .thenReturn(false);

        ResponseEntity<?> response = studyOrganizationController.deleteStudyOrganization(1L, 1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(studyOrganizationService, times(1)).deleteStudyOrganization(argThat(argument ->
                argument.getOrganizationId().equals(1L) && argument.getStudyId().equals(1L)));
    }

    /**
     * Tests the {@link StudyOrganizationController#deleteStudyOrganization(Long, Long)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testDeleteStudyOrganizationFailure() {
        when(studyOrganizationService.deleteStudyOrganization(argThat(argument ->
                argument.getOrganizationId().equals(1L) && argument.getStudyId().equals(1L))))
                .thenThrow(new RuntimeException("error"));

        ResponseEntity<?> response = studyOrganizationController.deleteStudyOrganization(1L, 1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("error", response.getBody());
        verify(studyOrganizationService, times(1)).deleteStudyOrganization(argThat(argument ->
                argument.getOrganizationId().equals(1L) && argument.getStudyId().equals(1L)));
    }
}


