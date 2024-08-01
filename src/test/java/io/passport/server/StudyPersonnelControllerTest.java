package io.passport.server;

import io.passport.server.controller.StudyPersonnelController;
import io.passport.server.model.Personnel;
import io.passport.server.service.StudyPersonnelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static io.passport.server.model.Role.DATA_ENGINEER;
import static io.passport.server.model.Role.STUDY_OWNER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StudyPersonnelController}.
 */
class StudyPersonnelControllerTest {

    @Mock
    private StudyPersonnelService studyPersonnelService;

    @InjectMocks
    private StudyPersonnelController studyPersonnelController;

    private Personnel personnel1;
    private Personnel personnel2;

    /**
     * Sets up test data and initializes mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        personnel1 = new Personnel("1", 1L, "John", "Doe", STUDY_OWNER, "john.doe@example.com");
        personnel2 = new Personnel("2", 1L, "Jane", "Doe", DATA_ENGINEER, "jane.doe@example.com");
    }

    /**
     * Tests the {@link StudyPersonnelController#getPersonnelByStudyId(Long, Long)}  method.
     * Verifies that all personnel for a study and an organization are returned with a status of 200 OK.
     */
    @Test
    void testGetPersonnelByStudyIdSuccess() {
        when(studyPersonnelService.findPersonnelByStudyIdAndOrganizationId(1L, 1L)).thenReturn(Arrays.asList(personnel1, personnel2));

        ResponseEntity<?> response = studyPersonnelController.getPersonnelByStudyId(1L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Arrays.asList(personnel1, personnel2), response.getBody());
        verify(studyPersonnelService, times(1)).findPersonnelByStudyIdAndOrganizationId(1L, 1L);
    }

    /**
     * Tests the {@link StudyPersonnelController#getPersonnelByStudyId(Long, Long)}  method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testGetPersonnelByStudyIdFailure() {
        when(studyPersonnelService.findPersonnelByStudyIdAndOrganizationId(1L,1L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = studyPersonnelController.getPersonnelByStudyId(1L, 1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studyPersonnelService, times(1)).findPersonnelByStudyIdAndOrganizationId(1L, 1L);
    }

    /**
     * Tests the {@link StudyPersonnelController#createStudyPersonnelEntries(Long, Long, List)} method.
     * Verifies that study personnel entries are created successfully with a status of 200 OK.
     */
    @Test
    void testCreateStudyPersonnelEntriesSuccess() {
        List<Personnel> personnel = Arrays.asList(personnel1, personnel2);
        doNothing().when(studyPersonnelService).createStudyPersonnelEntries(1L, 1L, personnel);
        when(studyPersonnelService.findPersonnelByStudyIdAndOrganizationId(1L, 1L)).thenReturn(personnel);

        ResponseEntity<?> response = studyPersonnelController.createStudyPersonnelEntries(1L, 1L, personnel);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(personnel, response.getBody());
        verify(studyPersonnelService, times(1)).createStudyPersonnelEntries(1L, 1L, personnel);
        verify(studyPersonnelService, times(1)).findPersonnelByStudyIdAndOrganizationId(1L, 1L);
    }

    /**
     * Tests the {@link StudyPersonnelController#createStudyPersonnelEntries(Long, Long, List)}  method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testCreateStudyPersonnelEntriesFailure() {
        List<Personnel> personnel = Arrays.asList(personnel1, personnel2);
        doThrow(new RuntimeException("Error")).when(studyPersonnelService).createStudyPersonnelEntries(1L, 1L, personnel);

        ResponseEntity<?> response = studyPersonnelController.createStudyPersonnelEntries(1L, 1L, personnel);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studyPersonnelService, times(1)).createStudyPersonnelEntries(1L, 1L, personnel);
    }
}
