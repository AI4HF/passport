package io.passport.server.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.passport.server.model.*;
import io.passport.server.service.*;
import org.keycloak.KeycloakPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class which stores the generated HTTP requests related to passport operations.
 */
@RestController
@RequestMapping("/passport")
public class PassportController {

    private static final Logger log = LoggerFactory.getLogger(PassportController.class);
    /**
     * Passport service for passport management
     */
    private final PassportService passportService;
    private final ModelDeploymentService modelDeploymentService;
    private final DeploymentEnvironmentService deploymentEnvironmentService;
    private final ModelService modelService;
    private final StudyService studyService;
    private final ParameterService parameterService;
    private final PopulationService populationService;
    private final SurveyService surveyService;
    private final ExperimentService experimentService;
    private final DatasetService datasetService;
    private final FeatureSetService featureSetService;
    private final LearningProcessService learningProcessService;
    private final LearningDatasetService learningDatasetService;
    private final LearningStageService learningStageService;
    private final FeatureService featureService;

    /**
     * Role checker service for authorization
     */
    private final RoleCheckerService roleCheckerService;

    @Autowired
    public PassportController(PassportService passportService, ModelDeploymentService modelDeploymentService,
                              DeploymentEnvironmentService deploymentEnvironmentService,
                              ModelService modelService,
                              StudyService studyService,
                              ParameterService parameterService,
                              PopulationService populationService,
                              SurveyService surveyService,
                              ExperimentService experimentService,
                              DatasetService datasetService,
                              FeatureSetService featureSetService,
                              LearningProcessService learningProcessService,
                              FeatureService featureService,
                              LearningStageService learningStageService,
                              LearningDatasetService learningDatasetService, RoleCheckerService roleCheckerService) {
        this.passportService = passportService;
        this.modelDeploymentService = modelDeploymentService;
        this.deploymentEnvironmentService = deploymentEnvironmentService;
        this.modelService = modelService;
        this.studyService = studyService;
        this.parameterService = parameterService;
        this.populationService = populationService;
        this.surveyService = surveyService;
        this.experimentService = experimentService;
        this.datasetService = datasetService;
        this.featureSetService = featureSetService;
        this.learningProcessService = learningProcessService;
        this.roleCheckerService = roleCheckerService;
        this.learningDatasetService = learningDatasetService;
        this.featureService = featureService;
        this.learningStageService = learningStageService;
    }

    /**
     * Read all passports
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @GetMapping()
    public ResponseEntity<List<Passport>> getAllPassportsByStudyId(@RequestParam Long studyId, @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> allowedRoles = List.of(Role.QUALITY_ASSURANCE_SPECIALIST);
        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Passport> passports = this.passportService.findPassportsByStudyId(studyId);

        long totalCount = passports.size();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        return ResponseEntity.ok().headers(headers).body(passports);
    }

    /**
     * Read a passport by passportId
     * @param passportId ID of the passport
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    /**@GetMapping("/{passportId}")
    public ResponseEntity<?> getPassport(@PathVariable Long passportId,
                                         @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> allowedRoles = List.of(Role.QUALITY_ASSURANCE_SPECIALIST);
        // Check role of the user
        if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Passport> passport = this.passportService.findPassportByPassportId(passportId);

        if(passport.isPresent()) {
            return ResponseEntity.ok().body(passport.get());
        }else{
            return ResponseEntity.notFound().build();
        }
    }**/

    /**
     * Delete passport by passportID.
     * @param passportId ID of the passport that is to be deleted.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    @DeleteMapping("/{passportId}")
    public ResponseEntity<?> deletePassport(@PathVariable Long passportId,
                                            @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try{

            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.QUALITY_ASSURANCE_SPECIALIST);
            // Check role of the user
            if(!this.roleCheckerService.hasAnyRole(principal, allowedRoles)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isDeleted = this.passportService.deletePassport(passportId);
            if(isDeleted) {
                return ResponseEntity.noContent().build();
            }else{
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Create Passport.
     * @param passport Passport model instance to be created.
     * @param principal KeycloakPrincipal object that holds access token
     * @return
     */
    /**@PostMapping()
    public ResponseEntity<?> createPassport(@RequestBody Passport passport,
                                            @AuthenticationPrincipal KeycloakPrincipal<?> principal) {
        try {
            // Allowed roles for this endpoint
            List<Role> allowedRoles = List.of(Role.QUALITY_ASSURANCE_SPECIALIST);
            if (!this.roleCheckerService.hasAnyRole(principal, allowedRoles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Retrieve the necessary details using deploymentId and studyId
            ModelDeployment deploymentDetails = modelDeploymentService.findModelDeploymentByDeploymentId(passport.getDeploymentId())
                    .orElseThrow(() -> new RuntimeException("Model deployment not found"));

            DeploymentEnvironment environmentDetails = deploymentEnvironmentService.findDeploymentEnvironmentById(deploymentDetails.getEnvironmentId())
                    .orElseThrow(() -> new RuntimeException("Deployment environment not found"));

            Model modelDetails = modelService.findModelById(deploymentDetails.getModelId())
                    .orElseThrow(() -> new RuntimeException("Model not found"));

            Study studyDetails = studyService.findStudyByStudyId(modelDetails.getStudyId())
                    .orElseThrow(() -> new RuntimeException("Study not found"));

            List<Parameter> parameters = parameterService.findParametersByStudyId(modelDetails.getStudyId());
            List<Population> populationDetails = populationService.findPopulationByStudyId(modelDetails.getStudyId());
            List<Survey> surveys = surveyService.findSurveysByStudyId(modelDetails.getStudyId());
            List<Experiment> experiments = experimentService.findExperimentByStudyId(modelDetails.getStudyId());

            // Populate FeatureSets with Features
            List<FeatureSetWithFeaturesDTO> featureSetsWithFeatures = featureSetService.getAllFeatureSetsByStudyId(modelDetails.getStudyId()).stream()
                    .map(featureSet -> {
                        List<Feature> features = featureService.findByFeaturesetId(featureSet.getFeaturesetId());
                        FeatureSetWithFeaturesDTO dto = new FeatureSetWithFeaturesDTO();
                        dto.setFeatureSet(featureSet);
                        dto.setFeatures(features);
                        return dto;
                    }).collect(Collectors.toList());

            // Populate Datasets with LearningDatasets
            List<DatasetWithLearningDatasetsDTO> datasetsWithLearningDatasets = datasetService.getAllDatasetsByStudyId(modelDetails.getStudyId()).stream()
                    .map(dataset -> {
                        List<LearningDataset> learningDatasets = learningDatasetService.findByDatasetId(dataset.getDatasetId());
                        DatasetWithLearningDatasetsDTO dto = new DatasetWithLearningDatasetsDTO();
                        dto.setDataset(dataset);
                        dto.setLearningDatasets(learningDatasets);
                        return dto;
                    }).collect(Collectors.toList());

            // Populate LearningProcesses with LearningStages
            List<LearningProcessWithStagesDTO> learningProcessesWithStages = learningProcessService.getAllLearningProcessByStudyId(modelDetails.getStudyId()).stream()
                    .map(learningProcess -> {
                        List<LearningStage> learningStages = learningStageService.findLearningStagesByProcessId(learningProcess.getLearningProcessId());
                        LearningProcessWithStagesDTO dto = new LearningProcessWithStagesDTO();
                        dto.setLearningProcess(learningProcess);
                        dto.setLearningStages(learningStages);
                        return dto;
                    }).collect(Collectors.toList());

            // Create PassportDetailsDTO
            PassportDetailsDTO passportDetailsDTO = new PassportDetailsDTO();
            passportDetailsDTO.setPassport(passport);
            passportDetailsDTO.setDeploymentDetails(deploymentDetails);
            passportDetailsDTO.setEnvironmentDetails(environmentDetails);
            passportDetailsDTO.setModelDetails(modelDetails);
            passportDetailsDTO.setStudyDetails(studyDetails);
            passportDetailsDTO.setParameters(parameters);
            passportDetailsDTO.setPopulationDetails(populationDetails);
            passportDetailsDTO.setSurveys(surveys);
            passportDetailsDTO.setExperiments(experiments);
            passportDetailsDTO.setFeatureSetsWithFeatures(convertObjectToJson(fetchFeatureSetsWithFeatures(passport)));
            passportDetailsDTO.setDatasetsWithLearningDatasets(convertObjectToJson(fetchDatasetsWithLearningDatasets(passport)));
            passportDetailsDTO.setLearningProcessesWithStages(convertObjectToJson(fetchLearningProcessesWithStages(passport)));


            // Save the basic passport and associated details
            passportService.savePassportDetails(passportDetailsDTO);

            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }**/

    @GetMapping("/details")
    public ResponseEntity<PassportDetailsDTO> getPassportDetails(@RequestParam Long passportId, @AuthenticationPrincipal KeycloakPrincipal<?> principal) {

        // Allowed roles for this endpoint
        List<Role> allowedRoles = List.of(Role.QUALITY_ASSURANCE_SPECIALIST);
        if (!this.roleCheckerService.hasAnyRole(principal, allowedRoles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Fetch the PassportDetailsDTO directly from the database by passportId
        PassportDetailsDTO passportDetailsDTO = passportService.findPassportDetailsByPassportId(passportId)
                .orElseThrow(() -> new RuntimeException("Passport details not found"));

        return ResponseEntity.ok(passportDetailsDTO);
    }

    // Helper method to convert object to JSON string
    private String convertObjectToJson(Object object) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting object to JSON", e);
        }
    }

    private List<FeatureSetWithFeaturesDTO> fetchFeatureSetsWithFeatures(Passport passport) {
        Long studyId = passport.getStudyId();

        // Fetch all feature sets by study ID
        List<FeatureSet> featureSets = featureSetService.getAllFeatureSetsByStudyId(studyId);

        // Convert each feature set into a DTO, adding the associated features
        return featureSets.stream().map(featureSet -> {
            List<Feature> features = featureService.findByFeaturesetId(featureSet.getFeaturesetId());
            return new FeatureSetWithFeaturesDTO(featureSet, features);
        }).collect(Collectors.toList());
    }

    private List<DatasetWithLearningDatasetsDTO> fetchDatasetsWithLearningDatasets(Passport passport) {
        Long studyId = passport.getStudyId();

        // Fetch all datasets by study ID
        List<Dataset> datasets = datasetService.getAllDatasetsByStudyId(studyId);

        // Convert each dataset into a DTO, adding the associated learning datasets
        return datasets.stream().map(dataset -> {
            List<LearningDataset> learningDatasets = learningDatasetService.findByDatasetId(dataset.getDatasetId());
            return new DatasetWithLearningDatasetsDTO(dataset, learningDatasets);
        }).collect(Collectors.toList());
    }


    private List<LearningProcessWithStagesDTO> fetchLearningProcessesWithStages(Passport passport) {
        Long studyId = passport.getStudyId();

        // Fetch all learning processes by study ID
        List<LearningProcess> learningProcesses = learningProcessService.getAllLearningProcessByStudyId(studyId);

        // Convert each learning process into a DTO, adding the associated learning stages
        return learningProcesses.stream().map(learningProcess -> {
            List<LearningStage> learningStages = learningStageService.findLearningStagesByProcessId(learningProcess.getLearningProcessId());
            return new LearningProcessWithStagesDTO(learningProcess, learningStages);
        }).collect(Collectors.toList());
    }

    /**
     * Endpoint to create a Passport and populate detailsJson field.
     *
     * @param passport The passport object with basic info (deploymentId, studyId, etc.).
     * @return The created Passport.
     */
    @PostMapping
    public ResponseEntity<Passport> createPassport(@RequestBody Passport passport) {
        Passport savedPassport = passportService.createPassport(passport);
        return ResponseEntity.status(201).body(savedPassport);
    }

    /**
     * Endpoint to retrieve Passport by passportId.
     *
     * @param passportId The ID of the passport.
     * @return The Passport object.
     */
    @GetMapping("/{passportId}")
    public ResponseEntity<Passport> getPassport(@PathVariable Long passportId) {
        Passport passport = passportService.getPassportById(passportId);
        return ResponseEntity.ok(passport);
    }


}
