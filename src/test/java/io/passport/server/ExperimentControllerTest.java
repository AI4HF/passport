package io.passport.server;

import io.passport.server.controller.ExperimentController;
import io.passport.server.model.Experiment;
import io.passport.server.service.ExperimentService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ExperimentController}.
 */
class ExperimentControllerTest {

    @Mock
    private ExperimentService experimentService;

    @InjectMocks
    private ExperimentController experimentController;

    private Experiment experiment1;
    private Experiment experiment2;

    /**
     * Sets up test data and initializes mocks before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        experiment1 = new Experiment(1L, 1L, "Research Question 1");
        experiment2 = new Experiment(2L, 1L, "Research Question 2");
    }

    /**
     * Tests the {@link ExperimentController#getExperimentsByStudyId(Long)} method.
     * Verifies that all experiments for a study are returned with a status of 200 OK.
     */
    @Test
    void testGetExperimentsByStudyId() {
        when(experimentService.findExperimentByStudyId(1L)).thenReturn(Arrays.asList(experiment1, experiment2));

        ResponseEntity<List<Experiment>> response = experimentController.getExperimentsByStudyId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        verify(experimentService, times(1)).findExperimentByStudyId(1L);
    }

    /**
     * Tests the {@link ExperimentController#createExperiments(Long, List)} method.
     * Verifies that experiments are created successfully with a status of 201 Created.
     */
    @Test
    void testCreateExperimentsSuccess() {
        List<Experiment> experiments = Arrays.asList(experiment1, experiment2);
        doNothing().when(experimentService).createExperimentEntries(1L, experiments);
        when(experimentService.findExperimentByStudyId(1L)).thenReturn(experiments);

        ResponseEntity<?> response = experimentController.createExperiments(1L, experiments);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(experiments, response.getBody());
        verify(experimentService, times(1)).createExperimentEntries(1L, experiments);
        verify(experimentService, times(1)).findExperimentByStudyId(1L);
    }

    /**
     * Tests the {@link ExperimentController#createExperiments(Long, List)} method.
     * Verifies that a status of 400 Bad Request is returned when an exception occurs.
     */
    @Test
    void testCreateExperimentsFailure() {
        List<Experiment> experiments = Arrays.asList(experiment1, experiment2);
        doThrow(new RuntimeException("Error")).when(experimentService).createExperimentEntries(1L, experiments);

        ResponseEntity<?> response = experimentController.createExperiments(1L, experiments);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(experimentService, times(1)).createExperimentEntries(1L, experiments);
    }
}
