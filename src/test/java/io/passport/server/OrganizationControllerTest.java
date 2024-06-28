package io.passport.server;

import io.passport.server.controller.OrganizationController;
import io.passport.server.model.Organization;
import io.passport.server.service.OrganizationService;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link OrganizationController}.
 */
class OrganizationControllerTest {

    @Mock
    private OrganizationService organizationService;

    @InjectMocks
    private OrganizationController organizationController;

    private Organization organization1;
    private Organization organization2;

    /**
     * Sets up test data and initializes mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        organization1 = new Organization(1L, "Organization 1", "Address 1");
        organization2 = new Organization(2L, "Organization 2", "Address 2");
    }

    /**
     * Tests the {@link OrganizationController#getAllOrganizations()} method.
     * Verifies that all organizations are returned with a status of 200 OK.
     */
    @Test
    void testGetAllOrganizations() {
        when(organizationService.getAllOrganizations()).thenReturn(Arrays.asList(organization1, organization2));

        ResponseEntity<List<Organization>> response = organizationController.getAllOrganizations();

        HttpHeaders headers = response.getHeaders();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("2", headers.getFirst("X-Total-Count"));
        verify(organizationService, times(1)).getAllOrganizations();
    }

    /**
     * Tests the {@link OrganizationController#getOrganizationById(Long)} method.
     * Verifies that an organization is returned with a status of 200 OK when found.
     */
    @Test
    void testGetOrganizationByIdFound() {
        when(organizationService.findOrganizationById(1L)).thenReturn(Optional.of(organization1));

        ResponseEntity<?> response = organizationController.getOrganizationById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Optional.of(organization1), response.getBody());
        verify(organizationService, times(1)).findOrganizationById(1L);
    }

    /**
     * Tests the {@link OrganizationController#getOrganizationById(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the organization is not found.
     */
    @Test
    void testGetOrganizationByIdNotFound() {
        when(organizationService.findOrganizationById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = organizationController.getOrganizationById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(organizationService, times(1)).findOrganizationById(1L);
    }

    /**
     * Tests the {@link OrganizationController#createOrganization(Organization)} method.
     * Verifies that an organization is created successfully with a status of 201 Created.
     */
    @Test
    void testCreateOrganizationSuccess() {
        when(organizationService.saveOrganization(organization1)).thenReturn(organization1);

        ResponseEntity<?> response = organizationController.createOrganization(organization1);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(organization1, response.getBody());
        verify(organizationService, times(1)).saveOrganization(organization1);
    }

    /**
     * Tests the {@link OrganizationController#createOrganization(Organization)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testCreateOrganizationFailure() {
        when(organizationService.saveOrganization(organization1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = organizationController.createOrganization(organization1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(organizationService, times(1)).saveOrganization(organization1);
    }

    /**
     * Tests the {@link OrganizationController#updateOrganization(Long, Organization)} method.
     * Verifies that an organization is updated successfully with a status of 200 OK when found.
     */
    @Test
    void testUpdateOrganizationFound() {
        when(organizationService.updateOrganization(1L, organization1)).thenReturn(Optional.of(organization1));

        ResponseEntity<?> response = organizationController.updateOrganization(1L, organization1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(organization1, response.getBody());
        verify(organizationService, times(1)).updateOrganization(1L, organization1);
    }

    /**
     * Tests the {@link OrganizationController#updateOrganization(Long, Organization)} method.
     * Verifies that a status of 404 Not Found is returned when the organization to update is not found.
     */
    @Test
    void testUpdateOrganizationNotFound() {
        when(organizationService.updateOrganization(1L, organization1)).thenReturn(Optional.empty());

        ResponseEntity<?> response = organizationController.updateOrganization(1L, organization1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(organizationService, times(1)).updateOrganization(1L, organization1);
    }

    /**
     * Tests the {@link OrganizationController#updateOrganization(Long, Organization)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testUpdateOrganizationFailure() {
        when(organizationService.updateOrganization(1L, organization1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = organizationController.updateOrganization(1L, organization1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(organizationService, times(1)).updateOrganization(1L, organization1);
    }

    /**
     * Tests the {@link OrganizationController#deleteOrganization(Long)} method.
     * Verifies that an organization is deleted successfully with a status of 204 No Content when found.
     */
    @Test
    void testDeleteOrganizationFound() {
        when(organizationService.deleteOrganization(1L)).thenReturn(true);

        ResponseEntity<?> response = organizationController.deleteOrganization(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(organizationService, times(1)).deleteOrganization(1L);
    }

    /**
     * Tests the {@link OrganizationController#deleteOrganization(Long)} method.
     * Verifies that a status of 404 Not Found is returned when the organization to delete is not found.
     */
    @Test
    void testDeleteOrganizationNotFound() {
        when(organizationService.deleteOrganization(1L)).thenReturn(false);

        ResponseEntity<?> response = organizationController.deleteOrganization(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(organizationService, times(1)).deleteOrganization(1L);
    }

    /**
     * Tests the {@link OrganizationController#deleteOrganization(Long)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testDeleteOrganizationFailure() {
        when(organizationService.deleteOrganization(1L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = organizationController.deleteOrganization(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(organizationService, times(1)).deleteOrganization(1L);
    }
}
