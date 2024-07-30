package io.passport.server;

import io.passport.server.controller.PersonnelController;
import io.passport.server.model.Credentials;
import io.passport.server.model.Personnel;
import io.passport.server.model.PersonnelDTO;
import io.passport.server.service.PersonnelService;
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

import static io.passport.server.model.Role.DATA_ENGINEER;
import static io.passport.server.model.Role.STUDY_OWNER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PersonnelController}.
 */
class PersonnelControllerTest {

    @Mock
    private PersonnelService personnelService;

    @InjectMocks
    private PersonnelController personnelController;

    private Personnel personnel1;
    private Personnel personnel2;
    private PersonnelDTO personnelDTO1;

    /**
     * Sets up test data and initializes mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        personnel1 = new Personnel("1", 1L, "John", "Doe", STUDY_OWNER, "john.doe@example.com");
        personnel2 = new Personnel("2", 1L, "Jane", "Doe", DATA_ENGINEER, "jane.doe@example.com");
        personnelDTO1 = new PersonnelDTO(personnel1, new Credentials());
    }

    /**
     * Tests the {@link PersonnelController#getPersonnelByOrganizationId(Optional)} method.
     * Verifies that all personnel for an organization are returned with a status of 200 OK.
     */
    @Test
    void testGetPersonnelByOrganizationId() {
        when(personnelService.findPersonnelByOrganizationId(1L)).thenReturn(Arrays.asList(personnel1, personnel2));

        ResponseEntity<List<Personnel>> response = personnelController.getPersonnelByOrganizationId(Optional.of(1L));

        HttpHeaders headers = response.getHeaders();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        assertEquals("2", headers.getFirst("X-Total-Count"));
        verify(personnelService, times(1)).findPersonnelByOrganizationId(1L);
    }

    /**
     * Tests the {@link PersonnelController#getPersonnelByPersonId(String)} method.
     * Verifies that personnel is returned with a status of 200 OK when found.
     */
    @Test
    void testGetPersonnelByPersonIdFound() {
        when(personnelService.findPersonnelById("1")).thenReturn(Optional.of(personnel1));

        ResponseEntity<?> response = personnelController.getPersonnelByPersonId("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Optional.of(personnel1), response.getBody());
        verify(personnelService, times(1)).findPersonnelById("1");
    }

    /**
     * Tests the {@link PersonnelController#getPersonnelByPersonId(String)} method.
     * Verifies that a status of 404 Not Found is returned when personnel is not found.
     */
    @Test
    void testGetPersonnelByPersonIdNotFound() {
        when(personnelService.findPersonnelById("1")).thenReturn(Optional.empty());

        ResponseEntity<?> response = personnelController.getPersonnelByPersonId("1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(personnelService, times(1)).findPersonnelById("1");
    }

    /**
     * Tests the {@link PersonnelController#createPersonnel(PersonnelDTO)} method.
     * Verifies that personnel is created successfully with a status of 201 Created.
     */
    @Test
    void testCreatePersonnelSuccess() {
        when(personnelService.savePersonnel(personnelDTO1)).thenReturn(Optional.of(personnel1));

        ResponseEntity<?> response = personnelController.createPersonnel(personnelDTO1);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(Optional.of(personnel1), response.getBody());
        verify(personnelService, times(1)).savePersonnel(personnelDTO1);
    }

    /**
     * Tests the {@link PersonnelController#createPersonnel(PersonnelDTO)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testCreatePersonnelFailure() {
        when(personnelService.savePersonnel(personnelDTO1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = personnelController.createPersonnel(personnelDTO1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(personnelService, times(1)).savePersonnel(personnelDTO1);
    }

    /**
     * Tests the {@link PersonnelController#updatePersonnel(String, Personnel)} method.
     * Verifies that personnel is updated successfully with a status of 200 OK when found.
     */
    @Test
    void testUpdatePersonnelFound() {
        when(personnelService.updatePersonnel("1", personnel1)).thenReturn(Optional.of(personnel1));

        ResponseEntity<?> response = personnelController.updatePersonnel("1", personnel1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(personnel1, response.getBody());
        verify(personnelService, times(1)).updatePersonnel("1", personnel1);
    }

    /**
     * Tests the {@link PersonnelController#updatePersonnel(String, Personnel)} method.
     * Verifies that a status of 404 Not Found is returned when the personnel to update is not found.
     */
    @Test
    void testUpdatePersonnelNotFound() {
        when(personnelService.updatePersonnel("1", personnel1)).thenReturn(Optional.empty());

        ResponseEntity<?> response = personnelController.updatePersonnel("1", personnel1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(personnelService, times(1)).updatePersonnel("1", personnel1);
    }

    /**
     * Tests the {@link PersonnelController#updatePersonnel(String, Personnel)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testUpdatePersonnelFailure() {
        when(personnelService.updatePersonnel("1", personnel1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = personnelController.updatePersonnel("1", personnel1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(personnelService, times(1)).updatePersonnel("1", personnel1);
    }

    /**
     * Tests the {@link PersonnelController#deletePersonnel(String)} method.
     * Verifies that personnel is deleted successfully with a status of 204 No Content when found.
     */
    @Test
    void testDeletePersonnelFound() {
        when(personnelService.deletePersonnel("1")).thenReturn(true);

        ResponseEntity<?> response = personnelController.deletePersonnel("1");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(personnelService, times(1)).deletePersonnel("1");
    }

    /**
     * Tests the {@link PersonnelController#deletePersonnel(String)} method.
     * Verifies that a status of 404 Not Found is returned when the personnel to delete is not found.
     */
    @Test
    void testDeletePersonnelNotFound() {
        when(personnelService.deletePersonnel("1")).thenReturn(false);

        ResponseEntity<?> response = personnelController.deletePersonnel("1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(personnelService, times(1)).deletePersonnel("1");
    }

    /**
     * Tests the {@link PersonnelController#deletePersonnel(String)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testDeletePersonnelFailure() {
        when(personnelService.deletePersonnel("1")).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = personnelController.deletePersonnel("1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(personnelService, times(1)).deletePersonnel("1");
    }
}
